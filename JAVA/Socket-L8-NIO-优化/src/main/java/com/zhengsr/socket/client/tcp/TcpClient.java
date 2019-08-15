package com.zhengsr.socket.client.tcp;

import com.zhengsr.socket.CloseUtils;
import com.zhengsr.socket.client.bean.DeviceInfo;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpClient {
    private final Socket socket;
    private final ReadHandler readHandler;
    private final PrintStream printStream;
    public TcpClient(Socket socket,ReadHandler readHandler) throws IOException {
        this.socket = socket;
        this.readHandler = readHandler;
        printStream = new PrintStream(socket.getOutputStream());
    }

    public void exit(){
        readHandler.exit();
        CloseUtils.close(printStream);
        CloseUtils.close(socket);
    }

    public static TcpClient bindwith(DeviceInfo info){
        try {
            Socket socket = new Socket();
            int timeout = 3000;
            socket.connect(new InetSocketAddress(InetAddress.getByName(info.ip),info.port),timeout);
            System.out.println("客户端已建立连接");
            System.out.println("客户端信息：" + socket.getLocalAddress() + " 端口:" + socket.getLocalPort());
            System.out.println("服务器信息：" + socket.getInetAddress() + " 端口:" + socket.getPort());
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            readHandler.start();
            return new TcpClient(socket,readHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
     */
    public void send(String msg) {
        printStream.println(msg);
    }




}
