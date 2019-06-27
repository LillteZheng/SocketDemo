package com.zhengsr.socketdemo;

import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;

/**
 * created by zhengshaorui
 * time on 2019/6/20
 */
public class UdpBroServer extends Thread {
    private static final String TAG = "UdpBroServer";
    private MulticastSocket socket ;
    private boolean isFinish = false;

    @Override
    public void run() {
      //  super.run();
        Log.d(TAG, "zsr UDP 服务端已经启动: ");
        try {
            //1.获取 datagramSocket 实例,并监听某个端口
            socket = new MulticastSocket(Constans.PORT);
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
                Log.d(TAG, "zsr 客户端: " + ip + "\tport: " + port + "\t信息: " + msg);

                /**
                 * 给客户端发送消息
                 */
                byte[] receiveMsg = "我是Android设备".getBytes();
                DatagramPacket receivePacket = new DatagramPacket(receiveMsg,
                        receiveMsg.length,
                        packet.getAddress(), //目标地址
                        Constans.BROADCAST_PORT);      //广播端口

                socket.send(receivePacket);
            }
            //关闭资源
            socket.close();
            Log.d(TAG, "zsr 结束");
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
