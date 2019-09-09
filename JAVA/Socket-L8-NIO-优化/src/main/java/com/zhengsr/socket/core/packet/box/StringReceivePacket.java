package com.zhengsr.socket.core.packet.box;

import com.zhengsr.socket.core.packet.ReceivePacket;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;

public class StringReceivePacket extends ReceivePacket<ByteArrayOutputStream> {
    private String string;
    public StringReceivePacket(int len) {
        length = len;
    }

    public String string() {
        return string;
    }

    @Override
    public void closeStream(ByteArrayOutputStream stream) throws IOException {
        super.closeStream(stream);
        string = new String(stream.toByteArray());
    }

    @Override
    public ByteArrayOutputStream createStream() {
        return new ByteArrayOutputStream((int) length);
    }
}
