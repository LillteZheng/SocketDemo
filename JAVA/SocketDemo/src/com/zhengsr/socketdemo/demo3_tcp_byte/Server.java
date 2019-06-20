package com.zhengsr.socketdemo.demo3_tcp_byte;

import com.zhengsr.socketdemo.Constans;

import java.io.IOException;
import java.net.*;

/**
 * created by zhengshaorui
 * time on 2019/6/20
 * 服务端，配置 tcp 和发送一些基础数据
 */
public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        initServerSocket(serverSocket);
        //backlog 表示可以等待的队列大小，加入有第51个客户端接入，则客户端会提示错误，一般不设置
        serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), Constans.PORT),50);

        System.out.println("服务器准备就绪～");
        System.out.println("服务器信息：" + serverSocket.getInetAddress() + " P:" + serverSocket.getLocalPort());

        //阻塞，拿到客户端socket
        Socket accept = serverSocket.accept();

        System.out.println("新客户端连接: "+accept.getInetAddress()+"\t[ort: "+accept.getPort());

        //拿到数据流
        accept.getInputStream();

    }


    private static void initServerSocket(ServerSocket serverSocket) throws SocketException {
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
