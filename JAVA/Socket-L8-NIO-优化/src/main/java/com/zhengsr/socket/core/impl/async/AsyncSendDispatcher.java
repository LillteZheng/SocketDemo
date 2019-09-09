package com.zhengsr.socket.core.impl.async;

import com.zhengsr.socket.CloseUtils;
import com.zhengsr.socket.core.IoArgs;
import com.zhengsr.socket.core.Sender;
import com.zhengsr.socket.core.packet.SendPacket;
import com.zhengsr.socket.core.packet.calback.SendDispatcher;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by @author zhengshaorui on 2019/8/19
 * Describe: 发送的具体逻辑类
 */
public class AsyncSendDispatcher implements SendDispatcher, IoArgs.IoArgsEventProcessor {
    private AtomicBoolean isSending = new AtomicBoolean();
    private AtomicBoolean isClosed = new AtomicBoolean();
    private Queue<SendPacket> queue = new ConcurrentLinkedDeque<>();

    private SendPacket<?> tempPacket;
    // 当前发送的packet的大小，以及进度
    private ReadableByteChannel packetChannel;
    private long position;
    private long total;
    private IoArgs ioArgs = new IoArgs();
    private Sender sender;
    public AsyncSendDispatcher(Sender sender) throws IOException {
        this.sender = sender;
        sender.setSendListener(this);
    }
    @Override
    public void send(SendPacket packet) {
        queue.offer(packet);
        if (isSending.compareAndSet(false,true)){
            sendNextMsg();
        }
    }

    private SendPacket takePacket(){
        SendPacket packet = queue.poll();
        if (packet != null && packet.isCanceled){
            //已取消，取下一条
            return takePacket();
        }
        return packet;
    }

    private void sendNextMsg() {
        //还有消息没发送完，先关闭
        SendPacket temp = tempPacket;
        if (temp != null){
            CloseUtils.close(temp);
        }


        SendPacket packet = tempPacket = takePacket();
        if (packet == null){
            //重置状态，可以继续发送了
            isSending.set(false);
            return;
        }

        position = 0;
        total = packet.length();
        sendCurrentPacket();

    }

    /**
     * 发送真正的数据,当能发送时，ioargs再填充数据，否则只是注册个发送的事件
     */
    private void sendCurrentPacket() {


        //判断是否发送完
        if (position >= total){
            onSendSuccessed(position == total);
            sendNextMsg();
            return;
        }

        try {
            sender.postSendAsync();
        } catch (IOException e) {
            closeAndNotify();
        }

    }

    private void onSendSuccessed(boolean isSuccess) {
        SendPacket packet = this.tempPacket;
        if (packet == null) {
            return;
        }
        CloseUtils.close(packet);
        CloseUtils.close(packetChannel);

        tempPacket = null;
        packetChannel = null;
        total = 0;
        position = 0;
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    @Override
    public void cancel(SendPacket packet) {

    }



    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false,true)){
            isSending.set(false);
           onSendSuccessed(false);
        }
    }

    @Override
    public IoArgs provideIoArgs() {
        //先拿到 ioArgs
        IoArgs args = ioArgs;
        //这个是首包，需要把长度信息写上，即占四个字节
        if (packetChannel == null){
            packetChannel = Channels.newChannel(tempPacket.open());
            args.limit(4);
            //todo 测试字符串，可以先用整型
            args.writeLength((int) tempPacket.length());
        }else{
            args.limit((int) Math.min(args.capacity(),total - position));
            try {
                int count = args.readFrom(packetChannel);
                position += count;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }


        return args;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        //通过这种循环，可以让一个大数据，根据 buffer 大小去发送
        sendCurrentPacket();
    }
}
