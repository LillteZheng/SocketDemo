package com.zhengsr.socketdemo.demo4_udp_tcp.client;

import com.zhengsr.socketdemo.Constans;
import com.zhengsr.socketdemo.demo4_udp_tcp.DeviceInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * created by zhengsr
 * time on 2019/6/23
 * 发送广播搜索UDP其他设备，被返回想要的ip
 */
public class UdpSearch {


    static DeviceInfo start(int timeout){
        try {
            //等待线程
            CountDownLatch searcheLatch = new CountDownLatch(1);
            ResposeListener listener = new ResposeListener(searcheLatch);
            listener.start();
            sendBroadcast();
            //只等待timeout s
            searcheLatch.await(timeout, TimeUnit.SECONDS);

            return listener.getDevices().get(0);

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 监听服务端发送回来的数据并打印出来
     */
    private static class ResposeListener extends Thread{
        private boolean isFinish = false;
        DatagramSocket socket;
        List<DeviceInfo> devices = new ArrayList<>();
        CountDownLatch searchLatch;
        public ResposeListener(CountDownLatch searchLatch) {
            this.searchLatch = searchLatch;
        }

        @Override
        public void run() {
            super.run();
            try {
                socket = new DatagramSocket(Constans.BROADCAST_PORT);
                System.out.println("开始监听");
                while(!isFinish) {
                    //监听回送端口
                    byte[] bytes = new byte[512];
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                    //拿数据
                    socket.receive(packet);

                    //拿到发送端的一些信息
                    String ip = packet.getAddress().getHostAddress();
                    int port = packet.getPort();
                    int length = packet.getLength();

                   // String msg = new String(buf, 0, length);
                    ByteBuffer buffer = ByteBuffer.wrap(bytes,0,length);
                    int cmd = buffer.getInt();
                    if (Constans.CMD_BRO_RESPONSE == cmd) {
                        int tcpPort = buffer.getInt();
                        int pos = buffer.position();
                        String msg = new String(bytes,pos,length - pos);
                        System.out.println("监听到: " + ip + "\ttcpPort: " + tcpPort + "\t信息: " + msg+" "+length+" "+pos);

                        if (msg.length() > 0) {
                            DeviceInfo device = new DeviceInfo(ip, tcpPort, msg);
                            devices.add(device);
                        }
                        //成功收取到一份
                        searchLatch.countDown();
                    }

                }

            }catch (Exception e){
              //  System.out.println(e.toString());
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

        public List<DeviceInfo> getDevices(){
            exit();
            return devices;
        }
    }




    /**
     * 发送广播
     * @throws IOException
     */
    public static void sendBroadcast() throws IOException {
        DatagramSocket socket = new DatagramSocket();

        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        //发送特定数据
        byteBuffer.putInt(Constans.CMD_BROAD);
        byteBuffer.putInt(Constans.BROADCAST_PORT);
        DatagramPacket packet = new DatagramPacket(byteBuffer.array(),
                byteBuffer.position(),
                InetAddress.getByName(Constans.BROADCAST_IP),
                Constans.PORT);
        socket.send(packet);
        socket.close();
    }
}
