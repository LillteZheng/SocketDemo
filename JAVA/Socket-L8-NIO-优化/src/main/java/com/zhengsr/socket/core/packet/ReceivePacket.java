package com.zhengsr.socket.core.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

public abstract class ReceivePacket<Stream extends OutputStream,Entity> extends Packet<Stream> {
    private Entity entity;
    public ReceivePacket(long len) {
        length = len;
    }

    /**
     * 得到最终接收到的数据实体
     *
     * @return 数据实体
     */
    public Entity entity() {
        return entity;
    }
    /**
     * 根据接收到的流转化为对应的实体
     *
     * @param stream {@link OutputStream}
     * @return 实体
     */
    protected abstract Entity buildEntity(Stream stream);

    /**
     * 先关闭流，随后将流的内容转化为对应的实体
     *
     * @param stream 待关闭的流
     * @throws IOException IO异常
     */
    @Override
    public void closeStream(Stream stream) throws IOException {
        super.closeStream(stream);
        entity = buildEntity(stream);
    }

}
