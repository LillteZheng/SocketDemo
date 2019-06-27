package com.zhengsr.chatroom.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * created by zhengshaorui
 * time on 2019/6/26
 */
public class Server {
    public static void main(String[] args) {
        UdpProvider.start();
        TcpServer tcpServer = new TcpServer();

        try {
            //noinspection ResultOfMethodCallIgnored
            //读取字符
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String msg;
            do {
                msg = reader.readLine();
                if (msg != null){
                    tcpServer.sendBroad(msg);
                }
            }while (!"bye".equalsIgnoreCase(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
        UdpProvider.stop();
        tcpServer.stop();

    }
}
