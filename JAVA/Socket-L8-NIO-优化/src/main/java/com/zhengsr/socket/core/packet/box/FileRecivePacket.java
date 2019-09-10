package com.zhengsr.socket.core.packet.box;

import com.zhengsr.socket.core.packet.ReceivePacket;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * created by @author zhengshaorui on 2019/9/10
 * Describe:
 */
public class FileRecivePacket extends ReceivePacket<FileOutputStream, File> {
    private final File file;
    public FileRecivePacket(long len,File file) {
        super(len);
        this.file = file;
    }

    @Override
    protected File buildEntity(FileOutputStream stream) {
        return file;
    }

    @Override
    public byte type() {
        return TYPE_STREAM_FILE;
    }

    @Override
    public FileOutputStream createStream() {
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
