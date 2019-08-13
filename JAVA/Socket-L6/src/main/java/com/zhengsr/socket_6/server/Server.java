package com.zhengsr.socket_6.server;

import com.zhengsr.socket_6.TCPConstants;
import com.zhengsr.socket_6.server.tcp.TcpServer;
import com.zhengsr.socket_6.server.udp.UdpProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {
    public static void main(String[] args) {
        TcpServer tcpServer = new TcpServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Start TCP server failed!");
            return;
        }
        UdpProvider.start(TCPConstants.PORT_SERVER);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String msg;
            do {
                msg = reader.readLine();
                if (msg != null) {
                    tcpServer.broadcastMsg(msg);
                }
            } while (!"bye".equalsIgnoreCase(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
        UdpProvider.stop();
        tcpServer.stop();
    }

}
