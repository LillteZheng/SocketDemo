package com.zhengsr.socketdemo.demo1;

import com.zhengsr.socketdemo.Constans;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * created by zhengshaorui
 * time on 2019/6/18
 * socket 客户端，从控制台获取信息，发送给服务器端，然后
 * 获取服务端的信息并显示
 */
public class Client {
    public static void main(String[] args) {

        try {
            //1.拿到socket实例
            Socket socket = new Socket();
            //超时时间
            socket.setSoTimeout(Constans.TIME_OUT);
            //连接本地，端口8000，超时时间为3s
            socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), Constans.PORT),Constans.TIME_OUT);
            //打印基本信息
            System.out.println("客服端信息: "+socket.getLocalAddress()+" 端口: "+socket.getLocalPort());
            System.out.println("服务端信息: "+socket.getInetAddress()+" 端口: "+socket.getPort());

            //获取终端输入流
            BufferedReader cmdReader = new BufferedReader(new InputStreamReader(System.in));
            //2.拿到 inputstream 或者 outputstream 进行通信
            PrintStream clientPrintStream = new PrintStream(socket.getOutputStream());
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //让它可以循环打印
            boolean flag = true;
            do {
                //获取终端数据，发送给服务器
                String msg = cmdReader.readLine();
                clientPrintStream.println(msg);

                //从服务器读数据
                String echo = clientReader.readLine();
                if ("bye".equalsIgnoreCase(echo)){
                    flag = false;
                }else{
                    System.out.println(echo);
                }


            }while (flag);

            //资源释放
            cmdReader.close();
            clientReader.close();
            //socket.close();
            System.out.println("我已断开连接");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
