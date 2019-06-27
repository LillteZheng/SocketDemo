package com.zhengsr.socketdemo.demo4_udp_tcp.server;

import com.zhengsr.socketdemo.Constans;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
/**
 * created by zhengshaorui
 * time on 2019/6/26
 */
public class TcpServer {

    private static ClientListener mClientListener;
    private static List<ClientDataHandle>  mClientHandles = new ArrayList<>();
    

    public TcpServer() {
        try {
            mClientListener = new ClientListener();
            mClientListener.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        mClientListener.exit();
        for (ClientDataHandle clientHandle : mClientHandles) {
            clientHandle.exit();
        }
        mClientHandles.clear();
    }



    public void sendBroad(String msg) {
        for (ClientDataHandle clientHandle : mClientHandles) {
            clientHandle.sendMsg(msg);
        }
    }

    static class ClientListener extends Thread {

        private boolean flag = true;
        private ServerSocket serverSocket;
        private Socket socket;
        public ClientListener() throws IOException {
             serverSocket = new ServerSocket(Constans.TCP_PORT);
        }

        @Override
        public void run() {
            super.run();
            try {
                do {
                    socket = serverSocket.accept();
                    System.out.println("新的客户端连接了: " + socket.getInetAddress() + " 端口: " + socket.getPort());
                    ClientDataHandle clientHandle = new ClientDataHandle(socket);
                    mClientHandles.add(clientHandle);
                }while (flag);


            } catch (IOException e) {
               // e.printStackTrace();
            } finally {
                exit();
            }

        }

        public void exit() {
            flag = false;
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                    serverSocket = null;
                }

                if (socket != null){
                    socket.close();
                    socket = null;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
