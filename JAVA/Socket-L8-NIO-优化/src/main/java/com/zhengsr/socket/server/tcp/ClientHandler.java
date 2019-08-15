package com.zhengsr.socket.server.tcp;

import com.sun.org.apache.bcel.internal.generic.Select;
import com.zhengsr.socket.CloseUtils;
import com.zhengsr.socket.core.Connector;

import java.awt.event.ItemEvent;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private ClientHandlerCallback handlerCallback;
    private SocketChannel socketChannel;
    private  ClientReadHandle readHandle;
    private final ClientWriteHandle writeHandle;
    private String clientInfo;
    private final Connector connector;

    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallback callback) throws IOException {
        handlerCallback = callback;
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);
        //读写用不同的 selector
        //Selector readSelector = Selector.open();
        //socketChannel.register(readSelector, SelectionKey.OP_READ);
       // readHandle = new ClientReadHandle(readSelector);

        connector = new Connector(){
            @Override
            public void onChannelClosed(SocketChannel channel) {
                super.onChannelClosed(channel);
                exitBySelf();
            }

            @Override
            protected void onReceiveNewMessage(String str) {
                super.onReceiveNewMessage(str);
                handlerCallback.onNewMessageArrived(ClientHandler.this,str);
            }
        };
        connector.setup(socketChannel);

        Selector writeSelector = Selector.open();
        socketChannel.register(writeSelector,SelectionKey.OP_WRITE);
        writeHandle = new ClientWriteHandle(writeSelector);
        this.clientInfo = socketChannel.getRemoteAddress().toString();

        System.out.println("新客户端连接: "+clientInfo);
    }

    private void exitBySelf(){
        exit();
        handlerCallback.onSelfClosed(this,"null");
    }

    public void exit(){
        readHandle.exit();
        writeHandle.exit();
        String msg = "客户端已退出：" + clientInfo;
        handlerCallback.onSelfClosed(this,msg);
        CloseUtils.close(socketChannel);
    }

    public String getInfo(){
        return clientInfo;
    }


    /**
     * 开始执行
     */
    public void readToPrint(){
        readHandle.start();
    }

    public void sendMsg(String msg){
        if (writeHandle != null){
            writeHandle.sendMsg(msg);
        }
    }


    /**
     * 读数据
     */
    class ClientReadHandle extends Thread{
        private boolean done;
        private Selector selector;
        private final ByteBuffer buffer;
        public ClientReadHandle(Selector selector) {
           this.selector = selector;
           buffer = ByteBuffer.allocate(256);
        }

        @Override
        public void run() {
            super.run();
            try {
                do {

                    if (selector.select() == 0){
                        if (done){
                            break;
                        }
                        continue;
                    }
                    //拿到已经准备好的事件集合
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()){
                        if (done){
                            break;
                        }

                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (key.isReadable()){
                            SocketChannel channel = (SocketChannel) key.channel();
                            //把数据都读到 bytebuffer 中
                            buffer.clear();
                            int read = channel.read(buffer);
                            if (read > 0) {
                                //去掉换行符
                                String msg = new String(buffer.array(), 0, buffer.position() - 1);
                                handlerCallback.onNewMessageArrived(ClientHandler.this,msg);
                            }else{
                                handlerCallback.onError("客户端没法读信息了");
                                ClientHandler.this.exit();
                                break;
                            }

                        }
                    }

                }while (!done);

            } catch (IOException e) {
                //不让它崩溃了，直接提示信息
               // e.printStackTrace();
                //还未退出
                if (!done) {
                    handlerCallback.onError("连接异常断开");
                    ClientHandler.this.exit();
                }

            }finally {
                CloseUtils.close(selector);
            }


        }

        public void exit(){
            done = true;
            selector.wakeup();
            CloseUtils.close(selector);
        }
    }

    class ClientWriteHandle {
        private ExecutorService sendExecutorService;
        private boolean done = false;
        private Selector selector;
        private final ByteBuffer buffer;
        public ClientWriteHandle(Selector selector) {
            this.selector = selector;
            buffer = ByteBuffer.allocate(256);
            sendExecutorService = Executors.newSingleThreadExecutor();
        }

        public void sendMsg(String msg){
            if (done){
                return;
            }
            sendExecutorService.execute(new sendRunnable(msg));
        }
        public void exit(){
            done = true;
            CloseUtils.close(selector);
            sendExecutorService.shutdownNow();
        }
        class sendRunnable implements Runnable{

            String msg;

            public sendRunnable(String msg) {
                this.msg = msg+"\n";
            }

            @Override
            public void run() {
                if (done){
                    return;
                }
                if (msg != null) {
                    //把数据填充到bytebuffer
                    buffer.clear();
                    buffer.put(msg.getBytes());
                    //切换到读模式，这样才能拿到数据
                    buffer.flip();
                    while (buffer.hasRemaining()){
                        //由于已经注册了写事件，则不用一直监听，所以这里用 socketchannel 就行，
                        //不用去阻塞和监听已经就绪的事件
                        try {
                            int write = socketChannel.write(buffer);
                            if (write < 0){
                                System.out.println("客户端已无法发送数据");
                                ClientHandler.this.exit();
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    /**
     * 接口监听一些数据
     */
    public interface ClientHandlerCallback {
        // 自身关闭通知
        void onSelfClosed(ClientHandler handler,String msg);

        // 收到消息时通知
        void onNewMessageArrived(ClientHandler handler, String msg);

        void onError(String msg);
    }
}
