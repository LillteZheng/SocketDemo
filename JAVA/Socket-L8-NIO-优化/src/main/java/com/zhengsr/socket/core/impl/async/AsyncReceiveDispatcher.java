package com.zhengsr.socket.core.impl.async;

import com.zhengsr.socket.CloseUtils;
import com.zhengsr.socket.core.IoArgs;
import com.zhengsr.socket.core.Receiver;
import com.zhengsr.socket.core.packet.ReceivePacket;
import com.zhengsr.socket.core.packet.box.StringReceivePacket;
import com.zhengsr.socket.core.packet.calback.ReceiverDispatcher;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by @author zhengshaorui on 2019/8/19
 * Describe:
 */
public class AsyncReceiveDispatcher implements ReceiverDispatcher {
    private AtomicBoolean isClosed = new AtomicBoolean(false);
    private Receiver receiver;
    private ReceivePacket tempPacket;
    private int position;
    private int total;
    private byte[] buffer;
    private ReceivePacketCallback callback;
    private IoArgs ioArgs = new IoArgs();
    public AsyncReceiveDispatcher(Receiver receiver,ReceivePacketCallback callback) {
        this.receiver = receiver;
        this.receiver.setReceiveListener(ioArgsEventListener);
        this.callback = callback;
    }

    @Override
    public void start() {
        registerReceive();
    }

    @Override
    public void stop() {

    }

    private void registerReceive(){
        try {
            receiver.receiveAsync(ioArgs);
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    /**
     * 解析数据
     * @param args
     */
    private void assemblePacket(IoArgs args){
        //首包
        if (tempPacket == null){
            int length = args.getLength();
            tempPacket = new StringReceivePacket(length);
            total = length;
            position = 0;
            buffer = new byte[length];
        }
        int count = args.writeTo(buffer,0);
        //有数据
        if (count > 0){
            //把数据存起来
            tempPacket.save(buffer,count);
            position += count;

            //检查是否已经接收完成
            if (position == total){
                completePacket();
            }
        }
    }

    private void completePacket() {
        ReceivePacket packet = tempPacket;
        CloseUtils.close(packet);
        callback.onReceivePacketCompleted(packet);
        tempPacket = null;
    }


    IoArgs.IoArgsEventListener ioArgsEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStart(IoArgs args) {
            int receiveSize;
            if (tempPacket == null){
                //头部长度，4个字节
                receiveSize = 4;
            }else{
                receiveSize = Math.min(total - position,args.capacity());
            }
            args.setLimit(receiveSize);
        }

        @Override
        public void onCompleted(IoArgs args) {
            //解析数据
            assemblePacket(args);
            //读下一条数据
            registerReceive();
        }
    };


    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false,true)){
            ReceivePacket packet = tempPacket;
            CloseUtils.close(packet);
            tempPacket = null;
        }
    }
}
