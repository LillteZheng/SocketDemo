package com.zhengsr.niodemo.chat;

import com.zhengsr.niodemo.Constants;
import sun.nio.cs.ext.MS874;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * created by zhengshaorui
 * time on 2019/07/02
 * 聊天客户端
 */
public class NioclientA {

    public static void main(String[] args) throws IOException {
        //1.创建 selector
        Selector selector = Selector.open();
        //2.创建SocketChannel
        SocketChannel socketChannel = SocketChannel.open();
        //3.设置为非阻塞模式
        socketChannel.configureBlocking(false);
        //4.连接服务器
        socketChannel.connect(new InetSocketAddress("localhost", Constants.PORT));
        //注册读事件，读取服户端信息
        socketChannel.register(selector, SelectionKey.OP_READ);
        //线程，监听服务器信息
        ReaderThread readerThread = new ReaderThread(selector);
        readerThread.start();
        //读取终端信息
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        boolean isFinish = false;
        System.out.println("客户端是否连接成功: "+socketChannel.finishConnect());
        if (socketChannel.finishConnect()) {
            while (!isFinish) {
                String msg = br.readLine();
                if ("bye".equals(msg)) {
                    isFinish = true;
                    sendData(socketChannel,"客户端A退出了");
                    readerThread.exit();
                    socketChannel.close();
                    System.out.println("服务端已退出");
                    break;
                } else {
                    sendData(socketChannel,msg);
                }

            }
        }
    }

    /**
     * 给服务器发送数据
     * @param channel
     * @param msg
     * @throws IOException
     */
    private static void sendData(SocketChannel channel,String msg) throws IOException {
        if (channel.isConnected()){
            channel.write(Charset.forName("utf-8").encode(msg));
        }
    }

    static class ReaderThread extends Thread{
        private Selector selector;
        private boolean isFinish = false;
        public ReaderThread(Selector selector) {
            this.selector = selector;
        }

        @Override
        public void run() {
            super.run();
            try {
                while (!isFinish){
                    //调用 select 方法，拿到 channel
                    int channels = selector.select();
                    if (channels == 0){
                        continue;
                    }
                    //如果能拿到，则通过 selectedKeys() 方法，拿到 selectedKeys 的集合
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()){
                        //拿到 selectedKeys 实例
                        SelectionKey selectionKey = iterator.next();
                        //如果是可读事件
                        if (selectionKey.isReadable()){
                            //拿到 就绪的 SocketChannel
                            SocketChannel channel = (SocketChannel) selectionKey.channel();
                            if (channel != null) {
                                //读取channel的数据
                                ByteBuffer buf = ByteBuffer.allocate(1024);
                                StringBuilder sb = new StringBuilder();
                                while (channel.read(buf) > 0) {
                                    //切换为读模式
                                    buf.flip();
                                    String msg = String.valueOf(Charset.forName("utf-8").decode(buf));
                                    sb.append(msg);
                                }
                                //将 channel 继续注册为可读事件
                                channel.register(selector, SelectionKey.OP_READ);
                                if (Constants.CLIENT_CONNECTED.equals(sb.toString())){
                                    sendData(channel,"我是客户端A");
                                }else {
                                    System.out.println(sb.toString());
                                }

                            }

                        }
                        iterator.remove();
                    }
                }
            } catch (IOException e) {
               // e.printStackTrace();
            }finally {
                exit();
            }
        }

        public void exit(){
            isFinish = true;
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
