package com.zhengsr.socket.core.packet.calback;

import com.zhengsr.socket.core.packet.ReceivePacket;

import java.io.Closeable;

/**
 * 接收的数据调度封装
 * 把一份或者多分IoArgs组合成一份Packet
 */
public interface ReceiverDispatcher extends Closeable {
    void start();

    void stop();

    interface ReceivePacketCallback {
        ReceivePacket<?,?> onArrivedNewPacket(byte type,long length);
        void onReceivePacketCompleted(ReceivePacket packet);
    }
}
