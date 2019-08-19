package com.zhengsr.socket.core.impl;

import com.zhengsr.socket.core.IoProvider;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * created by @author zhengshaorui on 2019/8/15
 * Describe: IoProvider 的实现类，读写用不同的 selector
 */
public class IoSelectorProvider implements IoProvider {
    private AtomicBoolean isClosed = new AtomicBoolean(false);
    //是否处于某个过程
    private AtomicBoolean inRegInput = new AtomicBoolean(false);
    private AtomicBoolean inRegOutput = new AtomicBoolean(false);
    private final Selector readSelector;
    private final Selector writeSelector;
    private final ExecutorService inputHandlePool;
    private final ExecutorService outputHandlePool;

    private HashMap<SelectionKey,Runnable> inputCallbackMap = new HashMap<>();
    private HashMap<SelectionKey,Runnable> outputCallbackMap = new HashMap<>();

    public IoSelectorProvider() throws IOException {
        readSelector = Selector.open();
        writeSelector = Selector.open();

        inputHandlePool = Executors.newFixedThreadPool(4,
                new IoProviderThreadFactory("IoProvider-Input-Thread-"));
        outputHandlePool = Executors.newFixedThreadPool(4,
                new IoProviderThreadFactory("IoProvider-Output-Thread-"));
        // 开始监听读写数据，注意这里是多线程模式,因为 IoSelectorProvider 已被设置成单利模式
        startRead();
        startWrite();
    }



    /**
     * 开一个线程，用来循环监听是否有数据被读到
     */
    private void startRead() {
        //开启
        Thread thread = new Thread("Clink IoSelectorProvider ReadSelector Thread"){
            @Override
            public void run() {
                super.run();
                while (!isClosed.get()){
                    try {
                        if (readSelector.select() == 0){
                            waitSelection(inRegInput);
                            continue;
                        }

                        Set<SelectionKey> selectionKeys = readSelector.selectedKeys();
                        //多线程处理方式，不直接遍历，避免数据阻塞
                        for (SelectionKey selectionKey : selectionKeys) {
                            //当前key是有效的
                            if (selectionKey.isValid()){
                                handleSelection(selectionKey,SelectionKey.OP_READ,inputCallbackMap,inputHandlePool);
                            }
                        }

                        selectionKeys.clear();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private void startWrite() {
        //开启
        Thread thread = new Thread("Clink IoSelectorProvider WriteSelector Thread"){
            @Override
            public void run() {
                super.run();
                while (!isClosed.get()){
                    try {
                        if (writeSelector.select() == 0){
                            waitSelection(inRegOutput);
                            continue;
                        }

                        Set<SelectionKey> selectionKeys = writeSelector.selectedKeys();
                        //多线程处理方式，不直接遍历，避免数据阻塞
                        for (SelectionKey selectionKey : selectionKeys) {
                            //当前key是有效的
                            if (selectionKey.isValid()){
                                handleSelection(selectionKey,SelectionKey.OP_WRITE,outputCallbackMap,outputHandlePool);
                            }
                        }

                        selectionKeys.clear();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    /**
     * 等待是否注册成功
     * @param locker
     */
    private static void waitSelection(AtomicBoolean locker){
        synchronized (locker){
            if (locker.get()){
                try {
                    locker.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 处理selector，把 runable 给线程池去处理
     * @param key
     * @param keyOps
     * @param map
     * @param pool
     */
    public static void handleSelection(SelectionKey key, int keyOps, HashMap<SelectionKey,Runnable> map,
                                       ExecutorService pool){
        /**
         * key.interestOps() 为感兴趣事件
         * 假如 interestOps 已经有读写事件了，即 OP_READ = 0X00000001 和 OP_WRITE = 0X00000100，
         * 那它们相加即为 0x00000101 ，如果要取消关注的事件，比如取消读事件，运算如下：
         * 0x00000101 & ~ 0x00000001 -> 0x00000101 & 0x11111110 = 0x00000100 就只剩下写事件了
         */
        //取消继续对 keyops 的监听
        key.interestOps(key.interestOps() & ~keyOps);

        Runnable runnable = null;
        try {
            runnable = map.get(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //从map中拿得到 runnable
        if (runnable != null && !pool.isShutdown()){
            //异步启动
            pool.execute(runnable);
        }
    }

    @Override
    public boolean registerInput(SocketChannel channel, HandleInputCallback callback) {
        return  registerSelection(channel,readSelector,
                SelectionKey.OP_READ,inRegInput,inputCallbackMap,callback) != null;
    }

    @Override
    public boolean registerOutput(SocketChannel channel, HandleOutputCallback callback) {
        return  registerSelection(channel,writeSelector,
                SelectionKey.OP_WRITE,inRegOutput,outputCallbackMap,callback) != null;
    }

    @Override
    public void unRegisterInput(SocketChannel channel) {
        unRegisterSelection(channel,readSelector,inputCallbackMap);
    }

    @Override
    public void unRegisterOutput(SocketChannel channel) {

        unRegisterSelection(channel,writeSelector,outputCallbackMap);
    }

    @Override
    public void close() throws IOException {
    }


    private static void unRegisterSelection(SocketChannel channel,Selector selector,
                                            HashMap<SelectionKey,Runnable> map){
        if (channel.isRegistered()){
            SelectionKey key = channel.keyFor(selector);
            if (key != null){
                /**
                 * 也可以用 key.interestOps(key.interestOps() & ~keyOps);
                 * 只是这里读写分离，所以用 key.cancel()也可以，如果是同个selector，
                 * 则需要区分一下读写
                 */
                key.cancel();
                map.remove(key);
                selector.wakeup();
            }
        }
    }

    /**
     * 注册 selection
     * @param channel
     * @param selector
     * @param registerOps
     * @param locker ，多线程，需要等待注册完成，才能去遍历和拿到数据，所以用原子锁
     * @param map
     * @param runnable
     * @return
     */
    private static SelectionKey registerSelection(SocketChannel channel,Selector selector,
                                                  int registerOps,AtomicBoolean locker,
                                                  HashMap<SelectionKey,Runnable> map,
                                                  Runnable runnable){
        synchronized (locker){
            //设置锁定状态
            locker.set(true);
            try {
                //唤醒 selector，让selector不处于 select() 状态
                selector.wakeup();
                //如果 channel 已经有注册过东西
                SelectionKey key = null;
                if (channel.isRegistered()){
                    key = channel.keyFor(selector);
                    //如果已经该 key 已经被注册过了
                    if (key != null){
                        //把key重新加入
                        key.interestOps(key.interestOps() | registerOps);
                    }
                }
                if (key == null){
                    //如果还没有被注册过
                    key = channel.register(selector, registerOps);
                    //并把当前的 key 和 runnable 填充到map
                    map.put(key,runnable);
                }

                return key;
            } catch (Exception e) {
               // e.printStackTrace();
            }finally {
                //解除锁定，表示注册完成
                locker.set(false);
            }
        }
        return null;
    }

    /**
     * The default thread factory
     */
    static class IoProviderThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        IoProviderThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
