package com.zhengsr.socketdemo.demo2_udp.upd_multicast;

import com.zhengsr.socketdemo.Constans;

import java.io.IOException;
import java.net.*;

public class UdpMultiCastServer {
    public static void main(String[] args) throws IOException {

        Provider provider = new Provider();
        provider.start();
        System.in.read();
        provider.exit();


    }

    static class Provider extends Thread{
        private MulticastSocket socket ;
        private boolean isFinish = false;
        private InetAddress group;

        public Provider() {

        }

        @Override
        public void run() {
            super.run();

            try {
                group = InetAddress.getByName("224.5.6.7");
                if (!group.isMulticastAddress()){
                    throw new RuntimeException("please use multicast ip 224.0.0.0 to 239.255.255.255 ");
                }
                System.out.println("组播服务端启动完成");
                socket = new MulticastSocket(Constans.PORT);
                //把组员加进来
                socket.joinGroup(group);

                while (!isFinish) {

                    //新建一个 package 来接受数据
                    byte[] bytes = new byte[512];
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                    socket.receive(packet);
                    String ip = packet.getAddress().getHostAddress();
                    int port = packet.getPort();
                    String msg = new String(bytes);
                    System.out.println("get client : " + ip + "\t port: " + port + "\tmsg: " + msg);
                    String receiveMsg = "hello "+msg;

                    byte[] buf = receiveMsg.getBytes();
                    DatagramPacket receivePacket = new DatagramPacket(buf,
                            buf.length,
                            packet.getAddress(),
                            port);
                    socket.send(receivePacket);
                }


            } catch (IOException e) {
                //  e.printStackTrace();
                //忽略错误
            }finally {
                exit();
            }
        }
        public void exit(){
            try {
                if (socket != null){
                    //离开这个组播
                    socket.leaveGroup(group);
                    socket.close();
                    socket = null;
                }
                isFinish = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
