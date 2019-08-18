package com.zhengsr.socket.core.packet;

public abstract class SendPacket extends Packet {

    public boolean isCanceled;

    public abstract byte[] bytes();

    public boolean isCanceled() {
        return isCanceled;
    }

}
