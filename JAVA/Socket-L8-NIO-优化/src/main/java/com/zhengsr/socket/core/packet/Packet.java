package com.zhengsr.socket.core.packet;

import java.io.Closeable;

/**
 * 公共的数据封装
 * 封装了类型和长度
 */
public abstract class Packet implements Closeable {
    protected byte type;
    protected int length;

    public byte type(){
        return  type;
    }

    public int length(){
        return length;
    }


}
