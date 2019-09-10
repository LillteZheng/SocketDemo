package com.zhengsr.socket.core.packet.box;

import com.zhengsr.socket.core.packet.ReceivePacket;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;

public class StringReceivePacket extends ByteRecivePacket {

    public StringReceivePacket(long len) {
        super(len);
    }

    @Override
    public byte type() {
        return TYPE_MEMORY_STRING;
    }

}
