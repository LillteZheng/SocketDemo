package com.zhengsr.socket.core.packet.calback;

import com.zhengsr.socket.core.packet.SendPacket;

import java.io.Closeable;


public interface SendDispatcher extends Closeable {

    void send(SendPacket packet);


    void cancel(SendPacket packet);
}
