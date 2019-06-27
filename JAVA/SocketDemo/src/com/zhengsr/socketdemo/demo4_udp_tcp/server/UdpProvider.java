package com.zhengsr.socketdemo.demo4_udp_tcp.server;

import com.zhengsr.socketdemo.Constans;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;
/**
 * created by zhengshaorui
 * time on 2019/6/26
 */
public class UdpProvider {

    private static Provider mProvider;

    static void start(){
        mProvider = new Provider();
        mProvider.start();
    }

    static void stop(){
        mProvider.exit();
    }

    static class Provider extends Thread{
        private DatagramSocket socket ;
        private boolean isFinish = false;
        public Provider() {
        }

        @Override
        public void run() {
            super.run();

            System.out.println("UDP 服务端已经启动");
            try {
                //1.获取 datagramSocket 实例,并监听某个端口
                socket = new DatagramSocket(Constans.PORT);
                while (!isFinish) {
                    //2.创建一个 udp_broad 的数据包
                    byte[] bytes = new byte[512];
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                    //3.开始阻塞获取udp数据包
                    socket.receive(packet);



                    //拿到发送端的一些信息
                    String ip = packet.getAddress().getHostAddress();
                    int port = packet.getPort();
                    int length = packet.getLength();

                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes,0,length);
                    int cmd = byteBuffer.getInt();
                    int responsePort = byteBuffer.getInt();
                    System.out.println("客户端: " + ip + "\tport: " + port +"\t回送接口: "+responsePort);
                    /**
                     * 给客户端发送消息  cmd 必须匹配
                     */
                    if (Constans.CMD_BROAD == cmd) {
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        buffer.putInt(Constans.CMD_BRO_RESPONSE);
                        buffer.putInt(Constans.TCP_PORT);
                        buffer.put(("我是设备: "+ UUID.randomUUID().toString()).getBytes());
                        DatagramPacket receivePacket = new DatagramPacket(buffer.array(),
                                buffer.position(),
                                packet.getAddress(), //目标地址
                                responsePort);      //广播端口

                        socket.send(receivePacket);
                    }
                }
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
