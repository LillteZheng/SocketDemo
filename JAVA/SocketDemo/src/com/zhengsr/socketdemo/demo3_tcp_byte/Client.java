package com.zhengsr.socketdemo.demo3_tcp_byte;

import com.zhengsr.socketdemo.Constans;

import java.io.IOException;
import java.net.*;

/**
 * created by zhengshaorui
 * time on 2019/6/20
 * tcp 客户端，传递基础数据 byte，char，int等
 */
public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = createSocket();
        configSocket(socket);
        //打印基础信息
        System.out.println("客服端信息: "+socket.getLocalAddress()+" 端口: "+socket.getLocalPort());
        System.out.println("服务端信息: "+socket.getInetAddress()+" 端口: "+socket.getPort());

        //连接服务器
        socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), Constans.PORT));

    }

    private static void configSocket(Socket socket)  throws IOException{
        // 设置读取超时时间为2秒
        socket.setSoTimeout(2000);

        // 是否复用未完全关闭的Socket地址，需要再 bind 之前有效
        socket.setReuseAddress(true);

        /**
         * 是否开启 nagle 算法，这里是关闭
         * tcp 每次发送数据都需要 ack 应答，如果关闭 nagle 算法，并不会每次新数据到来就立刻
         * 回送 ack，可能等到第二次或第三次再返回 ack，客户端在拿到 ack 之前，会一直等待，等
         * ack 回来之后，再把堆积的数据一次性发送给服务端，这样有助于弱网的情况
         */
        socket.setTcpNoDelay(true);

        // 是否需要在长时无数据响应时发送确认数据（类似心跳包），时间大约为2小时
        socket.setKeepAlive(true);

        // 对于close关闭操作行为进行怎样的处理；默认为false，0
        // false、0：默认情况，关闭时立即返回，底层系统接管输出流，将缓冲区内的数据发送完成
        // true、0：关闭时立即返回，缓冲区数据抛弃，直接发送RST结束命令到对方，并无需经过2MSL等待
        // true、200：关闭时最长阻塞200毫秒，随后按第二情况处理
        socket.setSoLinger(true, 200);

        // 是否让紧急数据内敛，默认false；紧急数据通过 socket.sendUrgentData(1);发送
        socket.setOOBInline(true);

        // 设置接收发送缓冲器大小
        socket.setReceiveBufferSize(64 * 1024 * 1024);
        socket.setSendBufferSize(64 * 1024 * 1024);

        // 设置性能参数：短链接，延迟，带宽的相对重要性,这里采用的权重的意思
        socket.setPerformancePreferences(1, 1, 0);
    }

    private static Socket createSocket() throws IOException {
         /*
        // 无代理模式，等效于空构造函数
        Socket socket = new Socket(Proxy.NO_PROXY);

        // 新建一份具有HTTP代理的套接字，传输数据将通过www.baidu.com:8080端口转发
        Proxy proxy = new Proxy(Proxy.Type.HTTP,
                new InetSocketAddress(Inet4Address.getByName("www.baidu.com"), 8800));
        socket = new Socket(proxy);

        // 新建一个套接字，并且直接链接到本地20000的服务器上
        socket = new Socket("localhost", PORT);

        // 新建一个套接字，并且直接链接到本地20000的服务器上
        socket = new Socket(Inet4Address.getLocalHost(), PORT);

        // 新建一个套接字，并且直接链接到本地20000的服务器上，并且绑定到本地20001端口上
        socket = new Socket("localhost", PORT, Inet4Address.getLocalHost(), LOCAL_PORT);
        socket = new Socket(Inet4Address.getLocalHost(), PORT, Inet4Address.getLocalHost(), LOCAL_PORT);
        */

        Socket socket = new Socket();
        // 绑定到本地20000端口,在 connect 连接服务器之前，这样 客户端的端口就不会再变动了
        socket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), 20000));

        return socket;
    }
}
