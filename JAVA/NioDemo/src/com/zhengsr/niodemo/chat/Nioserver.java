package com.zhengsr.niodemo.chat;

import com.zhengsr.niodemo.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by zhengshaorui
 * time on 2019/07/02
 * 聊天服务端
 */
public class Nioserver {
    public static void main(String[] args) throws IOException {
        //1.创建 selector
        Selector selector = Selector.open();
        //2.创建 ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //3.绑定端口
        serverSocketChannel.bind(new InetSocketAddress(Constants.PORT));
        //4.设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        //5.将channel注册到selector中
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务端启动成功,开始监听...");
        boolean isFinish = false;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        handleWrite(selector, br);
        while (!isFinish){
            //6.使用 select() 拿到channel
            int channels = selector.select();
            if (channels == 0){
                if (isFinish){
                    break;
                }
                continue;
            }
            // 7.通过selectedKeys() 拿到 selectedKeys 集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()){
                //拿到 selectedKeys 实例
                SelectionKey selectionKey = iterator.next();
                //移除 selectedKeys 实例
                iterator.remove();
                /**
                 * 如果是接入事件
                 */
                if (selectionKey.isAcceptable()){
                    handleAccept(serverSocketChannel,selector);
                }

                /**
                 * 如果是可读事件
                 */
                if (selectionKey.isReadable()){
                    handleRead(selectionKey,selector);
                }

                
            }
        }
    }

    private static void handleWrite(Selector selector, BufferedReader br)  {
        //开个线程去监听发送
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String msg = br.readLine();
                    broadcastMsg(selector,null,msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * 处理接受事件
     * @param serverSocketChannel
     * @param selector
     * @throws IOException
     */
    private static void handleAccept(ServerSocketChannel serverSocketChannel,Selector selector) throws IOException {
        //拿到 SocketChannel 客户端
        SocketChannel socketChannel = serverSocketChannel.accept();
        System.out.println("新客户端连接："+socketChannel.getRemoteAddress().toString());
        //设置 socketchannel 为非阻塞模式
        socketChannel.configureBlocking(false);
        //客户端注册读事件,这样我们才能接收到客户端的信息
        socketChannel.register(selector,SelectionKey.OP_READ);
        //发送 conected 提示服务端已经接收到
        ByteBuffer buf = Charset.forName("utf-8").encode(Constants.CLIENT_CONNECTED);
        socketChannel.write(buf);

    }

    /**
     * 处理客户端读事件，并广播出去
     * @param selectionKey
     * @param selector
     * @throws IOException
     */
    private static void handleRead(SelectionKey selectionKey,Selector selector) throws IOException {
        //拿到已经就绪的 channel
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        if (channel != null) {
            //读取channel的数据
            ByteBuffer buf = ByteBuffer.allocate(512);
            StringBuilder sb = new StringBuilder();
            int readByte = channel.read(buf);
            while (readByte > 0) {
                //切换为读模式
                buf.flip();
                String msg = String.valueOf(Charset.forName("utf-8").decode(buf));
                sb.append(msg);
                readByte = channel.read(buf);
            }
            buf.clear();
            //将 channel 继续注册为可读事件
            channel.register(selector, SelectionKey.OP_READ);
            if (sb.length() > 0) {
                System.out.println(channel.getRemoteAddress().toString()+" : " + sb.toString());
                 //返回数据
                //String responeMsg = sb.length();
                //channel.write(Charset.forName("utf-8").encode(responeMsg));
                //广播
                broadcastMsg(selector,channel,sb.toString());
            }

        }

    }


    /**
     * g
     * @param selector
     * @param targetChannel
     * @param msg
     * @throws IOException
     */
    private static void broadcastMsg(Selector selector,SocketChannel targetChannel,String msg) throws IOException {
        //拿到已连接的客户端个数
        Set<SelectionKey> keys = selector.keys();
        for (SelectionKey selectionKey : keys) {
            Channel channel =  selectionKey.channel();
            //不是自己本身,其他通道才需要拿到信息
            if (channel instanceof SocketChannel){
                if (targetChannel != null &&
                        channel == targetChannel ){
                    continue;
                }
                ((SocketChannel) channel).write(Charset.forName("utf-8").encode(msg));
            }
        }

    }
}
