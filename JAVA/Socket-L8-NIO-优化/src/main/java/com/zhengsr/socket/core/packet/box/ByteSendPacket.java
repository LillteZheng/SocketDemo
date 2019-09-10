package com.zhengsr.socket.core.packet.box;

import com.zhengsr.socket.core.packet.SendPacket;

import java.io.ByteArrayInputStream;

/**
 * created by @author zhengshaorui on 2019/9/10
 * Describe:
 */
public class ByteSendPacket extends SendPacket<ByteArrayInputStream> {

    private final byte[] bytes;

    public ByteSendPacket(byte[] bytes) {
        this.bytes = bytes;
        length = bytes.length;
    }

    @Override
    public byte type() {
        return TYPE_MEMORY_BYTES;
    }
    @Override
    public ByteArrayInputStream createStream() {
        return new ByteArrayInputStream(bytes);
    }

}
