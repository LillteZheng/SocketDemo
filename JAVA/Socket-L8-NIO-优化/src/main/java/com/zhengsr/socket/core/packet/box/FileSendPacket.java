package com.zhengsr.socket.core.packet.box;

import com.zhengsr.socket.core.packet.SendPacket;

import java.io.*;

public class FileSendPacket extends SendPacket<FileInputStream> {
    private final File file;
    public FileSendPacket(File file) {
        this.file = file;
        length = file.length();
    }


    @Override
    public byte type() {
        return TYPE_STREAM_FILE;
    }

    @Override
    public FileInputStream createStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


}
