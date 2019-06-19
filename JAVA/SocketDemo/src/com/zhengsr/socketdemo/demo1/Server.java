package com.zhengsr.socketdemo.demo1;

import com.zhengsr.socketdemo.Constans;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * created by zhengshaorui
 * time on 2019/6/18
 * socket 的服务端，拿到客户端信息后，返回客户端信息的长度
 */
public class Server {
    public static void main(String[] args) {


        try {
            //1.获取 serversocket 实例
            ServerSocket serverSocket = new ServerSocket(Constans.PORT);
            System.out.println("服务器准备就绪～");
            System.out.println("服务器信息：" + serverSocket.getInetAddress() + " P:" + serverSocket.getLocalPort());
            //开始阻塞接受客户端
            //2.通过 accept 拿到 客户端的socket
            Socket socket = serverSocket.accept();
            handleClient handleClient = new handleClient(socket);
            handleClient.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static class handleClient  extends Thread{

        private Socket socket;
        private boolean flag = true;
        public handleClient(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            try {
                System.out.println("新的客户端连接了: "+socket.getInetAddress()+" 端口: "+socket.getPort());
                //3.通过 inputstream 或 outputstream 进行数据传递
                PrintStream printStream = new PrintStream(socket.getOutputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                do {
                    String msg = br.readLine();
                    if ("bye".equalsIgnoreCase(msg)){
                        flag = false;
                        printStream.println("bye");
                    }else{
                        System.out.println("客户端信息: "+msg);
                        printStream.println("信息长度: "+msg.length());
                    }
                }while (flag);

                //释放资源
                br.close();

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            System.out.println("客户端: "+socket.getInetAddress()+" 断开连接了");
        }
    }
}
