package com.zhengsr.socket.core.packet.box;

import com.zhengsr.socket.core.packet.SendPacket;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class StringSendPacket extends SendPacket<ByteArrayInputStream> {

    public final byte[] bytes;

    public StringSendPacket(String msg) {
        this.bytes = msg.getBytes();
        length = bytes.length;
    }

    @Override
    public ByteArrayInputStream createStream() {
        return new ByteArrayInputStream(bytes);
    }
}
