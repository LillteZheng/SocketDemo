package com.zhengsr.socketdemo.udp_broadcast;

import com.zhengsr.socketdemo.Constans;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * created by zhengshaorui
 * time on 2019/6/19
 * 搜索有多个个服务端，即提供者
 */
public class BroadcastUdpClient {

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
     * 发送广播，我们只需要把ip改成 255.255.255.255 即可
     */
    private static void sendBroadcast()  {
        try {
            System.out.println("开始发送广播");
            //1.获取 datagramSocket 实例,不创建端口，客户端的端口由系统随机分配
            DatagramSocket socket = new DatagramSocket();
            //2.创建一个 udp 的数据包
            byte[] buf = "hello world".getBytes();

            DatagramPacket packet = new DatagramPacket(buf,
                    buf.length,
                    //InetAddress.getByName("172.16.29.255"),
                    InetAddress.getByName(Constans.BROADCAST_IP),
                    Constans.PORT);
            //给服务端发送数据
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }
}
