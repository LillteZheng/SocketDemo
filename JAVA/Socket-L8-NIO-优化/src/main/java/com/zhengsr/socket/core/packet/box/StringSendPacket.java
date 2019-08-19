package com.zhengsr.socket.core.packet.box;

import com.zhengsr.socket.core.packet.SendPacket;

import java.io.IOException;

public class StringSendPacket extends SendPacket {
    public final byte[] bytes;

    public StringSendPacket(String msg) {
        this.bytes = msg.getBytes();
        length = bytes.length;
    }

    @Override
    public byte[] bytes() {
        return bytes;
    }

    @Override
    public void close() throws IOException {

    }
}
