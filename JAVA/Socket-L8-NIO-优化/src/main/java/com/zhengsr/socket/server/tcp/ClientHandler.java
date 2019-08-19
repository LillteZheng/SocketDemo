package com.zhengsr.socket.server.tcp;

import com.sun.org.apache.bcel.internal.generic.Select;
import com.zhengsr.socket.CloseUtils;
import com.zhengsr.socket.core.Connector;

import java.awt.event.ItemEvent;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler extends Connector {
    private ClientHandlerCallback handlerCallback;
    private String clientInfo;

    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallback callback) throws IOException {
        handlerCallback = callback;
        setup(socketChannel);
        this.clientInfo = socketChannel.getRemoteAddress().toString();
        System.out.println("新客户端连接: "+clientInfo);
    }

    public void exit() {
        CloseUtils.close(this);
        System.out.println("客户端已退出：" + clientInfo);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        exitBySelf();
    }

    private void exitBySelf() {
        exit();
        handlerCallback.onSelfClosed(this,"客户端已关闭");
    }
    public String getInfo(){
        return clientInfo;
    }


    @Override
    protected void onReceiveNewMessage(String str) {
        super.onReceiveNewMessage(str);
        handlerCallback.onNewMessageArrived(this,str);
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
