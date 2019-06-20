package com.zhengsr.socketdemo.demo2_udp.udp_broadcast;

import com.zhengsr.socketdemo.Constans;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * created by zhengshaorui
 * time on 2019/6/19
 * upd 服务端，用来接收客户端信息
 */
public class BroadcastUdpServer {

    public static void main(String[] args) throws IOException {

        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();

        System.in.read();
        provider.exit();

    }

    static class Provider extends Thread{
        private String sn;
        private DatagramSocket socket ;
        private boolean isFinish = false;
        public Provider(String sn) {
            this.sn = sn;
        }

        @Override
        public void run() {
            super.run();

            System.out.println("UDP 服务端已经启动");
            try {
                //1.获取 datagramSocket 实例,并监听某个端口
                socket = new DatagramSocket(Constans.PORT);
                while (!isFinish) {
                    //2.创建一个 udp 的数据包
                    byte[] buf = new byte[512];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    //3.开始阻塞获取udp数据包
                    socket.receive(packet);

                    //拿到发送端的一些信息
                    String ip = packet.getAddress().getHostAddress();
                    int port = packet.getPort();
                    int length = packet.getLength();

                    String msg = new String(buf, 0, length);
                    System.out.println("客户端: " + ip + "\tport: " + port + "\t信息: " + msg);

                    /**
                     * 给客户端发送消息
                     */
                    byte[] receiveMsg = "我是设备A".getBytes();
                    DatagramPacket receivePacket = new DatagramPacket(receiveMsg,
                            receiveMsg.length,
                            packet.getAddress(), //目标地址
                            Constans.BROADCAST_PORT);      //广播端口

                    socket.send(receivePacket);
                }
                //关闭资源
                socket.close();
                System.out.println("结束");
            } catch (IOException e) {
              //  e.printStackTrace();
                //忽略错误
            }finally {
                exit();
            }
        }
        public void exit(){
            if (socket != null){
                socket.close();
                socket = null;
            }
            isFinish = true;
        }
    }
}
