package com.zhengsr.chatroom.client;

import com.zhengsr.chatroom.DeviceInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * created by zhengshaorui
 * time on 2019/6/26
 */
public class TcpClient {

    static void bindWith(DeviceInfo info){
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(InetAddress.getByName(info.ip),info.port));
            System.out.println("客户端已建立连接");
            System.out.println("客户端信息：" + socket.getLocalAddress() + " 端口:" + socket.getLocalPort());
            System.out.println("服务器信息：" + socket.getInetAddress() + " 端口:" + socket.getPort());

            ReaderListener listener = new ReaderListener(socket);
            listener.start();
            sendData(socket);


            listener.exit();
            System.out.println("客户端已退出");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送数据
     * @param socket
     */
    static void sendData(Socket socket){
        try {
            //终端输入流
            BufferedReader osReader = new BufferedReader(new InputStreamReader(System.in));
            PrintStream ps = new PrintStream(socket.getOutputStream());
            boolean isFinish = false;
            do {
                String msg = osReader.readLine();
                ps.println(msg);
                if ("bye".equalsIgnoreCase(msg)){
                    isFinish = true;
                }

            }while (!isFinish);
            osReader.close();
            ps.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听数据
     */
    static class ReaderListener extends Thread{
        Socket socket;
        boolean isFinish = false;
        public ReaderListener(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            try {
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                do {
                    String response = responseReader.readLine();
                    //当服务器关闭连接了，则 IO 不再阻塞，会返回null
                    if (response == null){
                        System.out.println("连接断开");
                        break;
                    }else {
                        if ("bye".equalsIgnoreCase(response)){
                            System.out.println("连接断开");
                            break;
                        }
                        System.out.println(response);
                    }
                }while (!isFinish);
                responseReader.close();
            } catch (IOException e) {
               // e.printStackTrace();
            }finally {
                exit();
            }

        }
        public void exit(){
            isFinish =true;
            if (socket != null){
                try {
                    socket.close();
                    socket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
