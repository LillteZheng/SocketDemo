package com.zhengsr.socket.core.packet;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * 公共的数据封装
 * 封装了类型和长度
 */
public abstract class Packet<T extends Closeable> implements Closeable {
    protected T stream;
    protected byte type;
    protected long length;

    public byte type(){
        return  type;
    }

    public long length(){
        return length;
    }


    public final T open(){
        if (stream == null){
            stream = createStream();
        }
        return stream;
    }

    @Override
    public final void close() throws IOException {
        if (stream != null){
            closeStream(stream);
            stream = null;
        }
    }

    /**
     * 子类需要自行创建 inputstream
     * @return
     */
    public abstract T createStream();

    public  void closeStream(T stream) throws IOException {
        stream.close();
    }


}
