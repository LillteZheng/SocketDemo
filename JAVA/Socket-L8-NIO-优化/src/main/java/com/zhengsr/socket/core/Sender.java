package com.zhengsr.socket.core;

import com.zhengsr.socket.core.IoArgs;

import java.io.Closeable;
import java.io.IOException;

public interface Sender extends Closeable {
    void setSendListener(IoArgs.IoArgsEventProcessor processor) throws IOException;
    boolean postSendAsync() throws IOException;
}
