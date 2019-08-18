package com.zhengsr.socket.core;

import com.zhengsr.socket.core.impl.SocketChannelAdapter;
import com.zhengsr.socket.core.packet.async.AsyncSendDispatcher;
import com.zhengsr.socket.core.packet.box.StringSendPacket;
import com.zhengsr.socket.core.packet.calback.SendDispatcher;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.security.Key;

public class Connector implements Closeable, SocketChannelAdapter.OnChannelStatusChangedListener {
    private SocketChannel socketChannel;
    private Sender sender;
    private Receiver receiver;
    private SendDispatcher sendDispatcher;
    public void setup(SocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;
        IoContext context = IoContext.get();
        SocketChannelAdapter adapter = new SocketChannelAdapter(socketChannel,this,context.getIoProvider());
        sender = adapter;
        receiver = adapter;

        sendDispatcher = new AsyncSendDispatcher(sender);

        readNextMessage();
    }

    private void readNextMessage(){
        if (receiver != null){
            try {
                receiver.receiverAsync(new IoArgs.IoArgsEventListener() {
                    @Override
                    public void onStart(IoArgs args) {

                    }

                    @Override
                    public void onCompleted(IoArgs args) {
                        onReceiveNewMessage(args.bufferString());
                        readNextMessage();
                    }
                });
            } catch (IOException e) {
                System.out.println("接收异常: "+e.toString());
            }
        }
    }

    public void sendMsg(String msg){
        StringSendPacket packet = new StringSendPacket(msg);
        sendDispatcher.send(packet);
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void onChannelClosed(SocketChannel channel) {

    }

    protected void onReceiveNewMessage(String str) {
    }
}
