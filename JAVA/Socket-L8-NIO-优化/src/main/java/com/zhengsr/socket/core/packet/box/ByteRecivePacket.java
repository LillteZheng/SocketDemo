package com.zhengsr.socket.core.packet.box;

import com.zhengsr.socket.core.packet.ReceivePacket;

import java.io.ByteArrayOutputStream;

/**
 * created by @author zhengshaorui on 2019/9/10
 * Describe:
 */
public class ByteRecivePacket extends ReceivePacket<ByteArrayOutputStream,byte[]> {

    public ByteRecivePacket(long len) {
        super(len);
    }

    @Override
    protected byte[] buildEntity(ByteArrayOutputStream stream) {
        return stream.toByteArray();
    }

    @Override
    public byte type() {
        return TYPE_MEMORY_BYTES;
    }

    @Override
    public ByteArrayOutputStream createStream() {
        return new ByteArrayOutputStream((int) length);
    }
}
