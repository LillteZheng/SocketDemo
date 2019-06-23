package com.zhengsr.socketdemo.demo4_udp_tcp;

import com.zhengsr.socketdemo.Constans;
import com.zhengsr.socketdemo.demo2_udp.udp_broadcast.BroadcastUdpClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * created by zhengsr
 * time on 2019/6/23
 * 发送广播搜索UDP其他设备，被返回想要的ip
 */
public class UdpSearch {

    public static void main(String[] args) throws IOException {
        ResposeListener listener = new ResposeListener(Constans.BROADCAST_PORT);
        listener.start();
        sendBroadcast();

        System.in.read();
        listener.exit();
        for (Device device : listener.getDevices()) {
            System.out.println("检测到的设备有: "+device.toString());
        }
    }


    /**
     * 监听服务端发送回来的数据并打印出来
     */
    private static class ResposeListener extends Thread{
        private int port;
        private boolean isFinish = false;
        DatagramSocket socket;
        List<Device> devices = new ArrayList<>();
        public ResposeListener(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            try {
                socket = new DatagramSocket(port);
                while(!isFinish) {
                    //监听回送端口
                    byte[] buf = new byte[512];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    //拿数据
                    socket.receive(packet);

                    //拿到发送端的一些信息
                    String ip = packet.getAddress().getHostAddress();
                    int port = packet.getPort();
                    int length = packet.getLength();

                    String msg = new String(buf, 0, length);
                    System.out.println("监听到: " + ip + "\tport: " + port + "\t信息: " + msg);

                    if (msg.length() > 0) {
                        Device device = new Device(ip, port, msg);
                        devices.add(device);
                    }

                }

            }catch (Exception e){
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

        public List<Device> getDevices(){
            exit();
            return devices;
        }
    }


    static class  Device{
        String ip;
        int port;
        String data;

        public Device(String ip, int port, String data) {
            this.ip = ip;
            this.port = port;
            this.data = data;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "ip='" + ip + '\'' +
                    ", port=" + port +
                    ", data='" + data + '\'' +
                    '}';
        }
    }

    /**
     * 发送广播
     * @throws IOException
     */
    public static void sendBroadcast() throws IOException {
        System.out.println("开始发送广播");
        DatagramSocket socket = new DatagramSocket();
        byte[] buf = "请发送你的ip".getBytes();
        DatagramPacket packet = new DatagramPacket(buf,
                buf.length,
                InetAddress.getByName(Constans.BROADCAST_IP),
                Constans.PORT);
        socket.send(packet);
        socket.close();
    }
}
