package com.zhengsr.socket.core.packet;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * 公共的数据封装
 * 封装了类型和长度
 */
public abstract class Packet<Stream extends Closeable> implements Closeable {

    // BYTES 类型
    public static final byte TYPE_MEMORY_BYTES = 1;
    // String 类型
    public static final byte TYPE_MEMORY_STRING = 2;
    // 文件 类型
    public static final byte TYPE_STREAM_FILE = 3;
    // 长链接流 类型
    public static final byte TYPE_STREAM_DIRECT = 4;

    protected Stream stream;

    public long length;



    public long length(){
        return length;
    }


    public final Stream open(){
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
     * 类型，直接通过方法得到:
     * <p>
     * {@link #TYPE_MEMORY_BYTES}
     * {@link #TYPE_MEMORY_STRING}
     * {@link #TYPE_STREAM_FILE}
     * {@link #TYPE_STREAM_DIRECT}
     *
     * @return 类型
     */
    public abstract byte type();

    /**
     * 子类需要自行创建 inputstream
     * @return
     */
    public abstract Stream createStream();



    public  void closeStream(Stream stream) throws IOException {
        stream.close();
    }


}
