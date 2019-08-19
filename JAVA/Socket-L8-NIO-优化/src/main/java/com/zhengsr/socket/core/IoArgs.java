package com.zhengsr.socket.core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class IoArgs {
    private static final int LIMIT = 128;
    private int limit = LIMIT;
    private byte[] byteBuffer = new byte[LIMIT];
    private ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);


    /**
     * 读数组数据到 buffer
     */
    public int readFrom(byte[] bytes,int offert){
        //拿到当前可填充的容量
        int size = Math.min(bytes.length - offert,buffer.remaining());
        buffer.put(bytes,offert,size);
        return size;
    }

    /**
     * 把buffer 数据写到 数组中
     */
    public int writeTo(byte[] bytes, int offert){

        //拿到当前可填充的容量
        int size = Math.min(bytes.length - offert,buffer.remaining());
        buffer.get(bytes,offert,size);
        return size;
    }

    public int readFrom(SocketChannel channel) throws IOException {

        startWriting();

        int length = 0;
        while (buffer.hasRemaining()){
            int read = channel.read(buffer);
            if (read < 0){
                throw new EOFException();
            }
            length += read;

        }

        finishWriting();
        return length;
    }

    public int writeTo(SocketChannel channel) throws IOException {
        int length = 0;
        while (buffer.hasRemaining()){
            int read = channel.write(buffer);
            if (read < 0){
                throw new EOFException();
            }
            length += read;

        }
        return length;
    }




    public void startWriting(){
        buffer.clear();
	    buffer.limit(limit);
    }

    public void finishWriting(){
        buffer.flip();
    }

    public void setLimit(int limit){
        this.limit = limit;
    }

    public void writeLength(int total) {
        buffer.putInt(total);
    }

    public int getLength(){
        return buffer.getInt();
    }

    public int capacity() {
        return buffer.capacity();
    }


    public interface IoArgsEventListener{
        void onStart(IoArgs args);
        void onCompleted(IoArgs args);
    }
}
