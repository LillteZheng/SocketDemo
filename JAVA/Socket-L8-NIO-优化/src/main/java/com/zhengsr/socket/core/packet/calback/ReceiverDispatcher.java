package com.zhengsr.socket.core.packet.calback;

import com.zhengsr.socket.core.packet.ReceivePacket;

public interface ReceiverDispatcher {
    void start();
    void stop();

    interface ReceivePacketCallback {
        void onReceivePacketCompleted(ReceivePacket packet);
    }
}
