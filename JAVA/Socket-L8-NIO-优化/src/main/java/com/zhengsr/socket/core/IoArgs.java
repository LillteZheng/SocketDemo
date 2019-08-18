package com.zhengsr.socket.core;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class IoArgs {
    private static final int LIMIT = 128;
    private int limit = LIMIT;
    private byte[] byteBuffer = new byte[LIMIT];
    private ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);




    public int read(SocketChannel channel) throws IOException {
        buffer.clear();
        return channel.read(buffer);
    }

    public String bufferString(){
        //去掉换行符
        return new String(byteBuffer,0,buffer.position() - 1);
    }

    public interface IoArgsEventListener{
        void onStart(IoArgs args);
        void onCompleted(IoArgs args);
    }
}
