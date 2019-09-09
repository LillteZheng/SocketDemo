package com.zhengsr.socket.core;

import java.io.Closeable;
import java.io.IOException;

public interface Receiver extends Closeable {
    void setReceiveListener(IoArgs.IoArgsEventProcessor processor) throws IOException;

    boolean postReceiveAsync() throws IOException;
}
