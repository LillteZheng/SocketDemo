package com.zhengsr.socket.core.packet.box;

import com.zhengsr.socket.core.packet.SendPacket;

import java.io.*;

public class FileSendPacket extends SendPacket<FileInputStream> {

    public FileSendPacket(File file) {

        length = file.length();
    }




    @Override
    public FileInputStream createStream() {
        return null;
    }


}
