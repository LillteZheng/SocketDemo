package com.zhengsr.socketdemo.demo3_tcp_byte;

import com.zhengsr.socketdemo.Constans;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * created by zhengshaorui
 * time on 2019/6/20
 * 服务端，配置 tcp 和发送一些基础数据
 */
public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = createServerSocket();
        configServerSocket(serverSocket);
        //backlog 表示可以等待的队列大小，加入有第51个客户端接入，则客户端会提示错误，一般不设置
        serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), Constans.PORT),50);

        System.out.println("服务器准备就绪～");
        System.out.println("服务器信息：" + serverSocket.getInetAddress() + " P:" + serverSocket.getLocalPort());

        //阻塞，拿到客户端socket
        Socket accept = serverSocket.accept();

        System.out.println("新客户端连接: "+accept.getInetAddress()+"\t[ort: "+accept.getPort());

        //拿到数据流
        InputStream inputStream = accept.getInputStream();
        byte[] buffer = new byte[256];
        int read = inputStream.read(buffer);
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer,0,read);
        byte b = byteBuffer.get();
        char aChar = byteBuffer.getChar();
        int anInt = byteBuffer.getInt();
        long aLong = byteBuffer.getLong();
        double aDouble = byteBuffer.getDouble();
        float aFloat = byteBuffer.getFloat();
        
        //拿到现在的  position
        int pos = byteBuffer.position();
        String msg = new String(buffer,pos,read - pos - 1);
        System.out.println("拿到基础数据：" + read + " 数据：" +"\n"
                + b + "\n"
                + aChar + "\n"
                + anInt + "\n"
                + aLong + "\n"
                + aDouble + "\n"
                + aFloat + "\n"
                + msg + "\n"
        );

        //回收给发送端
        OutputStream outputStream = accept.getOutputStream();
        outputStream.write(buffer,0,read);
        outputStream.close();
        serverSocket.close();

    }

    /**
     * 创建一个 serversocket
     * @return
     * @throws IOException
     */
    private static ServerSocket createServerSocket() throws IOException {
        // 创建基础的ServerSocket
        ServerSocket serverSocket = new ServerSocket();

        // 绑定到本地端口20000上，并且设置当前可允许等待链接的队列为50个
        //serverSocket = new ServerSocket(PORT);

        // 等效于上面的方案，队列设置为50个
        //serverSocket = new ServerSocket(PORT, 50);

        // 与上面等同
        // serverSocket = new ServerSocket(PORT, 50, Inet4Address.getLocalHost());

        return serverSocket;
    }

    /**
     * 配置 serversocket
     * @param serverSocket
     * @throws SocketException
     */
    private static void configServerSocket(ServerSocket serverSocket) throws SocketException {
        // 是否复用未完全关闭的地址端口
        serverSocket.setReuseAddress(true);

        // 设置接收buf，当大于64MB，则需要分片
        serverSocket.setReceiveBufferSize(64 * 1024 * 1024);

        // 设置serverSocket#accept超时时间，加入设置了2000，在 2s 内，没客户端接入，则 accept 报错
        // serverSocket.setSoTimeout(2000);

        // 设置性能参数：短链接，延迟，带宽的相对重要性,这里采用的权重的意思
        serverSocket.setPerformancePreferences(1, 1, 1);
    }
}
