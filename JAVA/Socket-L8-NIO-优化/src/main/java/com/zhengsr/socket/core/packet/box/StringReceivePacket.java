package com.zhengsr.socket.core.packet.box;

import com.zhengsr.socket.core.packet.ReceivePacket;

public class StringReceivePacket extends ReceivePacket {
    private final byte[] buffer;
    private int position;

    public StringReceivePacket(int len) {
        buffer = new byte[len];
        length = len;
    }

    @Override
    public void save(byte[] bytes, int count) {
        System.arraycopy(bytes,0,buffer,position,count);
        position += count;
    }

    public String string(){
        return new String(buffer);
    }
}
