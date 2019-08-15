package com.zhengsr.socket.client.udp;

import com.zhengsr.socket.CloseUtils;
import com.zhengsr.socket.UDPConstants;
import com.zhengsr.socket.client.bean.DeviceInfo;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UdpSearch {

    public static DeviceInfo searchServer(int timeout){
        try {
            CountDownLatch latch = new CountDownLatch(1);
            //先开启监听
            ResponseListener listener = startListener(latch);
            //发送广播
            sendBroadCast();
            //等待多少s之后，没收到，则提示
            latch.await(timeout, TimeUnit.MILLISECONDS);
            return listener.getInfo();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




    static ResponseListener startListener(CountDownLatch receiverLatch) throws InterruptedException {
        /**
         * 为什么这里要再加一个 CountDownLatch 呢？因为线程的启动时异步的，收到cpu的问题，可能此时run方法还没跑，
         * 就已经返回responseListener，导致有些数据没初始化好，所以，这里要加 CountDownLatch，确保线程已经正式启动
         */
        CountDownLatch startDownLatch = new CountDownLatch(1);
        ResponseListener responseListener = new ResponseListener(UDPConstants.PORT_CLIENT_RESPONSE,
                startDownLatch,receiverLatch);
        responseListener.start();
        startDownLatch.await();
        return responseListener;
    }


    static class ResponseListener extends Thread{
        private DatagramSocket ds = null;
        private final byte[] bytes = new byte[128];
        private boolean done = false;
        private int port;
        private DeviceInfo info;
        private CountDownLatch startLatch,receiverLatch;
        public ResponseListener(int port, CountDownLatch startLatch,CountDownLatch receiverLatch) {
            this.port = port;
            this.startLatch = startLatch;
            this.receiverLatch = receiverLatch;
        }

        @Override
        public void run() {
            super.run();
            //通知线程已经启动
            startLatch.countDown();
            try {
                ds = new DatagramSocket(port);
                DatagramPacket receivePack = new DatagramPacket(bytes,bytes.length);
                do {
                    ds.receive(receivePack);
                    String sendIp = receivePack.getAddress().getHostAddress();
                    int sendPort = receivePack.getPort();

                    System.out.println("udpsearch receive form ip:" + sendIp
                            + "\tport:" + sendPort );
                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                    int cmd = byteBuffer.getInt();
                    int responsePort = byteBuffer.getInt();

                    if (cmd == UDPConstants.RESPONSE && responsePort > 0){
                        info = new DeviceInfo(sendIp,responsePort,"server");
                        //接收到一份
                    }
                    receiverLatch.countDown();

                }while (!done);
            } catch (Exception e) {
               // e.printStackTrace();
               // System.out.println("error: "+e.toString());
            }finally {
                CloseUtils.close(ds);
            }
        }

        public DeviceInfo getInfo(){
            done = true;
            CloseUtils.close(ds);
            return info;
        }
    }

    /**
     * 发送UDP广播，接收端接收到消息之后，返回数tcp端口回来
     * @throws IOException
     */
    private static void sendBroadCast() throws IOException {
        //由系统指定端口
        DatagramSocket socket = new DatagramSocket();

        /**
         * 添加特定的数据包格式
         */
        ByteBuffer buffer = ByteBuffer.allocate(20);
        //添加头部
        //buffer.put(UDPConstants.HEADER);
        //添加命令
        buffer.putInt(UDPConstants.REQUEST);
        //添加端口
        buffer.putInt(UDPConstants.PORT_CLIENT_RESPONSE);

        /**
         * 构建packet
         */
        DatagramPacket packet = new DatagramPacket(
                buffer.array(),
                buffer.position(),
                InetAddress.getByName(UDPConstants.BROADCAST_IP),
                UDPConstants.PORT_SERVER//和服务端通信的端口，服务端也是通过该端口监听广播的
                );

        socket.send(packet);
        CloseUtils.close(socket);
        
        System.out.println("广播已发送");
    }
}
