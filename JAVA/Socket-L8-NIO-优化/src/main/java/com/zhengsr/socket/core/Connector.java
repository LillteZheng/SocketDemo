package com.zhengsr.socket.core;


import com.zhengsr.socket.core.impl.SocketChannelAdapter;
import com.zhengsr.socket.core.impl.async.AsyncReceiveDispatcher;
import com.zhengsr.socket.core.impl.async.AsyncSendDispatcher;
import com.zhengsr.socket.core.packet.Packet;
import com.zhengsr.socket.core.packet.ReceivePacket;
import com.zhengsr.socket.core.packet.SendPacket;
import com.zhengsr.socket.core.packet.box.ByteRecivePacket;
import com.zhengsr.socket.core.packet.box.FileRecivePacket;
import com.zhengsr.socket.core.packet.box.StringReceivePacket;
import com.zhengsr.socket.core.packet.box.StringSendPacket;
import com.zhengsr.socket.core.packet.calback.ReceiverDispatcher;
import com.zhengsr.socket.core.packet.calback.SendDispatcher;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public abstract class Connector implements Closeable, SocketChannelAdapter.OnChannelStatusChangedListener {
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

    public void sendPacket(SendPacket packet){
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


    protected void onReceivePacket(ReceivePacket packet) {
        System.out.println(":[New Packet]-Type:" + packet.type() + ", Length:" + packet.length);
    }
    public abstract File createNewReceiveFile();

    ReceiverDispatcher.ReceivePacketCallback receivePacketCallback = new ReceiverDispatcher.ReceivePacketCallback() {
        @Override
        public ReceivePacket<?, ?> onArrivedNewPacket(byte type, long length) {
            switch (type) {
                case Packet.TYPE_MEMORY_BYTES:
                    return new ByteRecivePacket(length);
                case Packet.TYPE_MEMORY_STRING:
                    return new StringReceivePacket(length);
                case Packet.TYPE_STREAM_FILE:
                    return new FileRecivePacket(length, createNewReceiveFile());
                case Packet.TYPE_STREAM_DIRECT:
                    return new ByteRecivePacket(length);
                default:
                    throw new UnsupportedOperationException("Unsupported packet type:" + type);
            }
        }

        @Override
        public void onReceivePacketCompleted(ReceivePacket packet) {
            onReceivePacket(packet);
        }
    };
}
