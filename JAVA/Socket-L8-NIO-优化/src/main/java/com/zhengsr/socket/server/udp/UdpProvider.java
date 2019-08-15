package com.zhengsr.socket.server.udp;

import com.zhengsr.socket.CloseUtils;
import com.zhengsr.socket.UDPConstants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

public class UdpProvider {


    private static Provider provider;

    public static void start(int port){
        stop();
        provider = new Provider(port);
        provider.start();
    }
    
    public static void stop(){
        if (provider != null){
            provider.exit();
        }
    }


    static class Provider extends Thread {
        byte[] sn;
        int port;
        boolean done = false;
        ByteBuffer byteBuffer;
        final byte[] bytes = new byte[20];
        DatagramSocket ds = null;

        public Provider(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("UDPProvider Started.");
            try {
                ds = new DatagramSocket(UDPConstants.PORT_SERVER);

                DatagramPacket receivePack = new DatagramPacket(bytes, bytes.length);
                do {
                    ds.receive(receivePack);

                    String clientIp = receivePack.getAddress().getHostAddress();
                    int clientPort = receivePack.getPort();

                    System.out.println("UDPProvider receive form ip:" + clientIp
                            + "\tport:" + clientPort);
                    byteBuffer = ByteBuffer.wrap(bytes);
                    int cmd = byteBuffer.getInt();
                    int responsePort = byteBuffer.getInt();

                    System.out.println("cmd: " + cmd + " " + responsePort);
                    if (cmd == UDPConstants.REQUEST && responsePort > 0) {
                        /**
                         * 回送一份数据，用来提示tcp连接的端口
                         */
                        ByteBuffer buffer = ByteBuffer.allocate(20);
                        buffer.putInt(UDPConstants.RESPONSE);
                        buffer.putInt(port);

                        DatagramPacket responsePacket = new DatagramPacket(
                                buffer.array(),
                                buffer.position(),
                                receivePack.getAddress(),
                                responsePort
                        );
                        ds.send(responsePacket);
                        System.out.println("已回送数据:" + clientIp + "\tport:" + responsePort + "\tdataLen:" + buffer.position());

                    }

                } while (!done);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                exit();
            }

        }

        public void exit() {
            done = true;
            CloseUtils.close(ds);
        }
    }


}
