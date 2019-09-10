package com.zhengsr.socket.core.impl.async;

import com.zhengsr.socket.CloseUtils;
import com.zhengsr.socket.core.IoArgs;
import com.zhengsr.socket.core.Receiver;
import com.zhengsr.socket.core.packet.Packet;
import com.zhengsr.socket.core.packet.ReceivePacket;
import com.zhengsr.socket.core.packet.box.StringReceivePacket;
import com.zhengsr.socket.core.packet.calback.ReceiverDispatcher;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by @author zhengshaorui on 2019/8/19
 * Describe:
 */
public class AsyncReceiveDispatcher implements ReceiverDispatcher, IoArgs.IoArgsEventProcessor {
    private AtomicBoolean isClosed = new AtomicBoolean(false);
    private Receiver receiver;
    private ReceivePacket<?,?> tempPacket;
    private WritableByteChannel packetChannel;
    private long position;
    private long total;
    private ReceivePacketCallback callback;
    private IoArgs ioArgs = new IoArgs();
    public AsyncReceiveDispatcher(Receiver receiver,ReceivePacketCallback callback) throws IOException {
        this.receiver = receiver;
        this.receiver.setReceiveListener(this);
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
            receiver.postReceiveAsync();
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
            int length = args.readLength();
            //解析，是文件类型还是字符创类型
            byte type = length > 200 ? Packet.TYPE_STREAM_FILE : Packet.TYPE_MEMORY_STRING;
            //tempPacket = new StringReceivePacket(length);
            tempPacket = callback.onArrivedNewPacket(type,length);
            packetChannel = Channels.newChannel(tempPacket.open());
            total = length;
            position = 0;
        }
        try {
            int count = args.writeTo(packetChannel);
            //把数据存起来
            position += count;

            //检查是否已经接收完成
            if (position == total){
                completePacket(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            completePacket(false);
        }

    }

    private void completePacket(boolean isSuccess) {
        ReceivePacket packet = tempPacket;
        CloseUtils.close(packet);
        tempPacket = null;
        WritableByteChannel channel = this.packetChannel;
        CloseUtils.close(channel);
        packetChannel = null;
        if (packet != null) {
            callback.onReceivePacketCompleted(packet);
        }
    }



    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false,true)){
            completePacket(false);
        }
    }

    @Override
    public IoArgs provideIoArgs() {
        IoArgs args = ioArgs;
        int receiveSize;
        if (tempPacket == null){
            //头部长度，4个字节
            receiveSize = 4;
        }else{
            receiveSize = (int) Math.min(total - position,args.capacity());
        }
        args.limit(receiveSize);
        return args;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        //解析数据
        assemblePacket(args);
        //读下一条数据
        registerReceive();
    }
}
