package com.zhengsr.socket.core.packet.calback;

import com.zhengsr.socket.core.packet.SendPacket;

public interface SendDispatcher {
    void send(SendPacket packet);

    void cancel(SendPacket packet);
}
