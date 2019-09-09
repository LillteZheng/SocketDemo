package com.zhengsr.socket.core.packet;

import java.io.InputStream;

public abstract class SendPacket<T extends InputStream> extends Packet<T> {

    public boolean isCanceled;


    public boolean isCanceled() {
        return isCanceled;
    }

}
