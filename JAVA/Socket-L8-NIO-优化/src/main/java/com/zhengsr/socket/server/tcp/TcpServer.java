package com.zhengsr.socket.server.tcp;

import com.zhengsr.socket.CloseUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer implements ClientHandler.ClientHandlerCallback {
    int port;
    private final ExecutorService forwordingThreadPool;
    private ClientListener clientListener;
    private List<ClientHandler> mClientHandlers = new ArrayList<>();
    private Selector selector;
    private ServerSocketChannel server;

    public TcpServer(int port){
        this.port = port;
        //转发线程池
        forwordingThreadPool = Executors.newSingleThreadExecutor();
    }

    public boolean start(){
        try {
            //创建selector 和 serversocketchannel
            selector = Selector.open();
            server = ServerSocketChannel.open();
            //配置非阻塞模式
            server.configureBlocking(false);
            //绑定端口
            server.bind(new InetSocketAddress(port));
            //注册接入事件
            server.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("服务器信息：" + server.getLocalAddress().toString());
            //监听客户端
            clientListener = new ClientListener();
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
        CloseUtils.close(server);
        CloseUtils.close(selector);
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

        private boolean done = false;

        @Override
        public void run() {
            super.run();
            System.out.println("服务器准备就绪~");
            do {
                try {
                    //等待循环，防止cpu 100% 空转的问题
                    if (selector.select() == 0){
                        if (done){
                            break;
                        }
                        continue;
                    }

                    //拿到准备就绪的事件
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()){
                        if (done){
                            break;
                        }
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        //如果是接入事件
                        if (key.isAcceptable()){
                            ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                            //拿到客户端
                            SocketChannel socketChannel = channel.accept();

                            //客户端构建读写异步线程
                            ClientHandler clientHandle = new ClientHandler(socketChannel,TcpServer.this);
                           // clientHandle.readToPrint();
                            //同步，把客户端添加进来
                            synchronized (TcpServer.this){
                                mClientHandlers.add(clientHandle);
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }while (!done);
        }

        public void exit(){
            done = true;
            //唤醒selector，防止其他事件阻塞
            selector.wakeup();
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
