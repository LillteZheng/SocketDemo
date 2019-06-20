package com.zhengsr.socketdemo.upd_multicast;

import com.zhengsr.socketdemo.Constans;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UdpMultiCastClientB {
    public static void main(String[] args) {
        try {
            InetAddress group = InetAddress.getByName("224.5.6.8");
            if (!group.isMulticastAddress()){
                throw new RuntimeException("please use multicast ip 224.0.0.0 to 239.255.255.255 ");
            }
            System.out.println("客户端B已启动");
            //客户端的端口由系统自行指定
            MulticastSocket socket = new MulticastSocket();

            byte[] buf = (Constans.SN_HEADER+"B").getBytes();
            DatagramPacket packet = new DatagramPacket(buf,
                    buf.length,
                    InetAddress.getLocalHost(),
                    Constans.PORT);
            socket.send(packet);

            byte[] bytes = new byte[512];
            DatagramPacket receivePacket = new DatagramPacket(bytes,bytes.length);
            socket.receive(receivePacket);
            String ip = receivePacket.getAddress().getHostAddress();
            int port = receivePacket.getPort();
            String msg = new String(bytes);
            System.out.println("get server : "+ip+"\t port: "+port+"\tmsg: "+msg);
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
