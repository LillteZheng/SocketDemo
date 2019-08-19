package com.zhengsr.socket.core;


import com.zhengsr.socket.core.impl.SocketChannelAdapter;
import com.zhengsr.socket.core.impl.async.AsyncReceiveDispatcher;
import com.zhengsr.socket.core.impl.async.AsyncSendDispatcher;
import com.zhengsr.socket.core.packet.box.StringReceivePacket;
import com.zhengsr.socket.core.packet.box.StringSendPacket;
import com.zhengsr.socket.core.packet.calback.ReceiverDispatcher;
import com.zhengsr.socket.core.packet.calback.SendDispatcher;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Connector implements Closeable, SocketChannelAdapter.OnChannelStatusChangedListener {
    private SocketChannel socketChannel;
    private Sender sender;
    private Receiver receiver;
    private SendDispatcher sendDispatcher;
    private ReceiverDispatcher receiverDispatcher;
    public void setup(SocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;
        IoContext context = IoContext.get();
        SocketChannelAdapter adapter = new SocketChannelAdapter(socketChannel,this,context.getIoProvider());
        sender = adapter;
        receiver = adapter;

        sendDispatcher = new AsyncSendDispatcher(sender);
        receiverDispatcher = new AsyncReceiveDispatcher(receiver,receivePacketCallback);
        receiverDispatcher.start();
    }



    public void sendMsg(String msg){
        StringSendPacket packet = new StringSendPacket(msg);
        sendDispatcher.send(packet);
    }

    @Override
    public void close() throws IOException {
        receiverDispatcher.close();
        sendDispatcher.close();
        sender.close();
        receiver.close();
        socketChannel.close();
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {

    }

    protected void onReceiveNewMessage(String str) {
        //System.out.println("什么啊: "+": "+str);
    }


    ReceiverDispatcher.ReceivePacketCallback receivePacketCallback = packet -> {
        if (packet instanceof StringReceivePacket){
            String str = ((StringReceivePacket) packet).string();
            onReceiveNewMessage(str);
        }
    };
}
