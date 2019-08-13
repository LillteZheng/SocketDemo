package com.zhengsr.socket_6.server.tcp;

import com.zhengsr.socket_6.CloseUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

public class TcpServer implements ClientHandler.ClientHandlerCallback {
    int port;
    private final ExecutorService forwordingThreadPool;
    private ClientListener clientListener;
    private List<ClientHandler> mClientHandlers = new ArrayList<>();
    public TcpServer(int port){
        this.port = port;
        //转发线程池
        forwordingThreadPool = Executors.newSingleThreadExecutor();
    }

    public boolean start(){
        try {
            clientListener = new ClientListener(port);
            clientListener.start();
        } catch (IOException e) {
           // e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop(){
        if (clientListener !=null){
            clientListener.exit();
        }
        synchronized (TcpServer.this){
            for (ClientHandler handler : mClientHandlers) {
                handler.exit();
            }
        }
        mClientHandlers.clear();
        forwordingThreadPool.shutdownNow();
    }


    public synchronized void broadcastMsg(String msg){
        for (ClientHandler handler : mClientHandlers) {
            handler.sendMsg(msg);
        }
    }


    /**
     * 监听客户端
     */
    class ClientListener extends Thread{

        private ServerSocket server;
        private boolean done = false;
        public ClientListener(int port) throws IOException {
            server = new ServerSocket(port);
            System.out.println("服务器信息：" + server.getInetAddress() + " P:" + server.getLocalPort());
        }

        @Override
        public void run() {
            super.run();
            System.out.println("服务器准备就绪~");
            do {
                try {
                    //得到客户端
                    Socket client = server.accept();
                    //客户端构建读写异步线程
                    ClientHandler clientHandle = new ClientHandler(client,TcpServer.this);
                    clientHandle.readToPrint();
                    //同步，把客户端添加进来
                    synchronized (TcpServer.this){
                        mClientHandlers.add(clientHandle);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }




            }while (!done);
        }

        public void exit(){
            done = true;
            CloseUtils.close(server);
            System.out.println("服务器已关闭！");
        }
    }


    @Override
    public synchronized void onSelfClosed(ClientHandler handler, String msg) {
        System.out.println("onSelfClosed: "+msg);
        mClientHandlers.remove(handler);
    }

    @Override
    public void onNewMessageArrived(ClientHandler handler, String msg) {
        //打印出来
        System.out.println(handler.getInfo()+" "+msg);

        //异步转发数据
        forwordingThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (TcpServer.this){
                    for (ClientHandler clientHandler : mClientHandlers) {
                        //跳过自身
                        if (clientHandler == handler){
                            continue;
                        }
                        clientHandler.sendMsg(msg);
                    }
                }
            }
        });
    }

    @Override
    public void onError(String msg) {
        System.out.println("onError: "+msg);
    }
}
