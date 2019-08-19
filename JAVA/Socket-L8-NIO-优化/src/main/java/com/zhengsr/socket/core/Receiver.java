package com.zhengsr.socket.core;

import java.io.Closeable;
import java.io.IOException;

public interface Receiver extends Closeable {
    void setReceiveListener(IoArgs.IoArgsEventListener listener);

    boolean receiveAsync(IoArgs args) throws IOException;
}
