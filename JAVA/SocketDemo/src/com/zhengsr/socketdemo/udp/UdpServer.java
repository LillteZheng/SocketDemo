package com.zhengsr.socketdemo.udp;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import com.zhengsr.socketdemo.Constans;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * created by zhengshaorui
 * time on 2019/6/19
 * upd 服务端，用来接收客户端信息
 */
public class UdpServer {

    public static void main(String[] args) throws IOException {
        System.out.println("UDP 服务端已经启动");
        //1.获取 datagramSocket 实例,并监听某个端口
        DatagramSocket socket = new DatagramSocket(Constans.PORT);
        //2.创建一个 udp 的数据包
        byte[] buf = new byte[512];
        DatagramPacket packet = new DatagramPacket(buf,buf.length);
        //3.开始阻塞获取udp数据包
        socket.receive(packet);

        //拿到发送端的一些信息
        String ip = packet.getAddress().getHostAddress();
        int port = packet.getPort();
        int length = packet.getLength();

        String msg = new String(buf,0,length);
        System.out.println("客户端: "+ip+"\tport: "+port+"\t信息: "+msg);

        /**
         * 给客户端发送消息
         */
        byte[] receiveMsg = ("长度: "+msg.length()).getBytes();
        DatagramPacket receivePacket = new DatagramPacket(receiveMsg,
                receiveMsg.length,
                packet.getAddress(), //目标地址
                port);               //目标端口

        socket.send(receivePacket);
        //关闭资源
        socket.close();
        System.out.println("结束");
    }
}
