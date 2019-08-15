package com.zhengsr.socket.core;

import java.io.Closeable;
import java.io.IOException;

public interface Receiver extends Closeable {
    boolean receiverAsync(IoArgs.IoArgsEventListener listener) throws IOException;
}
