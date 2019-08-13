package com.zhengsr.socket_6.client.tcp;

import com.zhengsr.socket_6.CloseUtils;
import com.zhengsr.socket_6.client.bean.DeviceInfo;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class TcpClient {

    public static void bindwith(DeviceInfo info){
        try {
            Socket socket = new Socket();
            int timeout = 3000;
            socket.connect(new InetSocketAddress(InetAddress.getByName(info.ip),info.port),timeout);
            System.out.println("客户端已建立连接");
            System.out.println("客户端信息：" + socket.getLocalAddress() + " 端口:" + socket.getLocalPort());
            System.out.println("服务器信息：" + socket.getInetAddress() + " 端口:" + socket.getPort());
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            readHandler.start();
            send(socket);
            readHandler.exit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    static class  ReadHandler extends Thread{
        private BufferedReader br;
        private boolean done = false;
        public ReadHandler(InputStream inputStream) {
            br = new BufferedReader(new InputStreamReader(inputStream));
        }

        @Override
        public void run() {
            super.run();
            try {
                while (!done){
                    String msg = br.readLine();
                    if (msg == null){
                        System.out.println("连接断开");
                        break;
                    }
                    if ("bye".equals(msg)){
                        break;
                    }
                    System.out.println("接收到信息: "+msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        public void exit(){
            done = true;
            CloseUtils.close(br);
        }
    }

    /**
     * 接受终端数据，并发送给服务端
     * @param client
     */
    private static void send(Socket client){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            PrintStream ps = new PrintStream(client.getOutputStream());
            while (true){
                String msg = br.readLine();
                ps.println(msg);
                if ("bye".equals(msg)){
                    break;
                }
            }
            br.close();
            ps.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
