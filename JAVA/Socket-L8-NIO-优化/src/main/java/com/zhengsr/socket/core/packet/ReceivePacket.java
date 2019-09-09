package com.zhengsr.socket.core.packet;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class ReceivePacket<T extends OutputStream> extends Packet<T> {
}
