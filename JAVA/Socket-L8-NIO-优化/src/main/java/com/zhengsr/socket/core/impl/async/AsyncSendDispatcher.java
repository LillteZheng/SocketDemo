package com.zhengsr.socket.core.impl.async;

import com.zhengsr.socket.CloseUtils;
import com.zhengsr.socket.core.IoArgs;
import com.zhengsr.socket.core.Sender;
import com.zhengsr.socket.core.packet.SendPacket;
import com.zhengsr.socket.core.packet.calback.SendDispatcher;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by @author zhengshaorui on 2019/8/19
 * Describe: 发送的具体逻辑类
 */
public class AsyncSendDispatcher implements SendDispatcher {
    private AtomicBoolean isSending = new AtomicBoolean();
    private AtomicBoolean isClosed = new AtomicBoolean();
    private Queue<SendPacket> queue = new ConcurrentLinkedDeque<>();

    private SendPacket tempPacket;
    private int position;
    private int total;
    private IoArgs ioArgs = new IoArgs();
    private Sender sender;
    public AsyncSendDispatcher(Sender sender){
        this.sender = sender;
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
     * 发送真正的数据
     */
    private void sendCurrentPacket() {
        //先拿到 ioArgs
        IoArgs args = ioArgs;

        args.startWriting();

        //判断是否发送完
        if (position >= total){
            sendNextMsg();
            return;
        }
        //这个是首包，需要把长度信息写上，即占四个字节
        if (position == 0){
            args.writeLength(total);
        }
        //拿到真正的数据

        byte[] bytes = tempPacket.bytes();
        //把bytes数据写到 ioargs
        int count = args.readFrom(bytes, position);

        //记录标志，如果buffer不够，则继续填充数据
        position += count;

        args.finishWriting();

        try {
            sender.sendAsync(args,sendIoArgsEventListener);
        } catch (IOException e) {
            closeAndNotify();
        }

    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    @Override
    public void cancel(SendPacket packet) {

    }

    IoArgs.IoArgsEventListener sendIoArgsEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStart(IoArgs args) {

        }

        @Override
        public void onCompleted(IoArgs args) {
            //通过这种循环，可以让一个大数据，根据 buffer 大小去发送
            sendCurrentPacket();
        }
    };

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false,true)){
            isSending.set(false);
            SendPacket packet = tempPacket;
            CloseUtils.close(packet);
            tempPacket = null;
        }
    }
}
