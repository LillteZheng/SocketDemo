package com.zhengsr.socket.core.packet.async;

import com.zhengsr.socket.CloseUtils;
import com.zhengsr.socket.core.IoArgs;
import com.zhengsr.socket.core.Sender;
import com.zhengsr.socket.core.packet.SendPacket;
import com.zhengsr.socket.core.packet.calback.SendDispatcher;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncSendDispatcher implements SendDispatcher {
    private AtomicBoolean isSending = new AtomicBoolean();
    private Queue<SendPacket> queue = new ConcurrentLinkedDeque<>();

    private SendPacket tempPacket;
    private int position;
    private int total;
    private IoArgs ioArgs = new IoArgs();

    public AsyncSendDispatcher(Sender sender){

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
            //已取消，不用发送了
            return takePacket();
        }
        return packet;
    }

    private void sendNextMsg() {
        SendPacket temp = tempPacket;
        if (temp != null){
            CloseUtils.close(temp);
        }

        SendPacket packet = tempPacket = takePacket();
        if (packet == null){
            isSending.set(false);
        }

        position = 0;
        
        sendCurrentPacket();

    }

    private void sendCurrentPacket() {

    }

    @Override
    public void cancel(SendPacket packet) {

    }
}
