package com.zhengsr.socket_6.server.tcp;

import com.zhengsr.socket_6.CloseUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private ClientHandlerCallback handlerCallback;
    private Socket socket;
    private final ClientReadHandle readHandle;
    private final ClientWriteHandle writeHandle;
    private String clientInfo;

    public ClientHandler(Socket socket, ClientHandlerCallback callback) throws IOException {
        handlerCallback = callback;
        this.socket = socket;
        readHandle = new ClientReadHandle(socket.getInputStream());
        writeHandle = new ClientWriteHandle(socket.getOutputStream());
        this.clientInfo = "A[" + socket.getInetAddress().getHostAddress()
                + "] P[" + socket.getPort() + "]";

        System.out.println("新客户端连接: "+clientInfo);
    }

    public void exit(){
        readHandle.exit();
        writeHandle.exit();
        String msg = "客户端已退出：" + socket.getInetAddress() +
                " P:" + socket.getPort();
        handlerCallback.onSelfClosed(this,msg);
        CloseUtils.close(socket);
    }

    public String getInfo(){
        return clientInfo;
    }


    /**
     * 开始执行
     */
    public void readToPrint(){
        readHandle.start();
    }

    public void sendMsg(String msg){
        if (writeHandle != null){
            writeHandle.sendMsg(msg);
        }
    }


    /**
     * 读数据
     */
    class ClientReadHandle extends Thread{
        private boolean done;
        private InputStream inputStream;
        private BufferedReader br;

        public ClientReadHandle(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            super.run();
            br = new BufferedReader(new InputStreamReader(inputStream));
            try {
                do {
                    String msg = br.readLine();
                    if (msg == null){
                        handlerCallback.onError("客户端没法读信息了");
                        ClientHandler.this.exit();
                        break;
                    }
                    handlerCallback.onNewMessageArrived(ClientHandler.this,msg);
                }while (!done);

            } catch (IOException e) {
                //不让它崩溃了，直接提示信息
               // e.printStackTrace();
                //还未退出
                if (!done) {
                    handlerCallback.onError("连接异常断开");
                    ClientHandler.this.exit();
                }

            }finally {
                CloseUtils.close(br);
                CloseUtils.close(inputStream);
            }


        }

        public void exit(){
            done = true;
            CloseUtils.close(br);
            CloseUtils.close(inputStream);
        }
    }

    class ClientWriteHandle {
        private PrintStream ps = null;
        private ExecutorService sendExecutorService;
        private boolean done = false;
        public ClientWriteHandle(OutputStream outputStream) {
            ps = new PrintStream(outputStream);
            sendExecutorService = Executors.newSingleThreadExecutor();
        }

        public void sendMsg(String msg){
            if (done){
                return;
            }
            sendExecutorService.execute(new sendRunnable(msg));
        }
        public void exit(){
            done = true;
            CloseUtils.close(ps);
            sendExecutorService.shutdownNow();
        }
        class sendRunnable implements Runnable{

            String msg;

            public sendRunnable(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                if (msg != null && !ClientWriteHandle.this.done) {
                    ps.println(msg);
                }
            }
        }
    }


    /**
     * 接口监听一些数据
     */
    public interface ClientHandlerCallback {
        // 自身关闭通知
        void onSelfClosed(ClientHandler handler,String msg);

        // 收到消息时通知
        void onNewMessageArrived(ClientHandler handler, String msg);

        void onError(String msg);
    }
}
