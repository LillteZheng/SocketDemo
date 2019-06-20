package com.zhengsr.socketdemo.demo2_udp.udp;

import com.zhengsr.socketdemo.Constans;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * created by zhengshaorui
 * time on 2019/6/19
 * udp 的客户端
 */
public class UdpClient {
    public static void main(String[] args) throws IOException {
        System.out.println("UDP 客户端已经启动");
        //1.获取 datagramSocket 实例,不创建端口，客户端的端口由系统随机分配
        DatagramSocket socket = new DatagramSocket();
        //2.创建一个 udp 的数据包
        byte[] buf = "hello world".getBytes();

        DatagramPacket packet = new DatagramPacket(buf,
                buf.length,
                InetAddress.getLocalHost(),
                Constans.PORT);
        //给服务端发送数据
        socket.send(packet);

        /**
         * 接收服务端消息
         */
        byte[] receiveMsg = new byte[512];
        DatagramPacket receivePacket = new DatagramPacket(receiveMsg, receiveMsg.length);
        //开始接收
        socket.receive(receivePacket);
        String address = packet.getAddress().getHostAddress();
        int port = packet.getPort();
        int length = packet.getLength();

        String msg = new String(receiveMsg,0,length);
        System.out.println("服务端: "+address+"\tport: "+port+"\t信息: "+msg);
        //关闭资源
        socket.close();
        System.out.println("结束");
    }
}
