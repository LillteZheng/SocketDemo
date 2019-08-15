package com.zhengsr.socket.core;

import java.io.IOException;

/**
 * created by @author zhengshaorui on 2019/8/15
 * Describe: 上下文，主要用来初始化 IoProvider
 */
public class IoContext {
    private final IoProvider ioProvider;
    private static IoContext INSTANCE;
    public IoContext(IoProvider ioProvider) {
        this.ioProvider = ioProvider;
    }

    public IoProvider getIoProvider() {
        return ioProvider;
    }
    public static IoContext get(){
        return INSTANCE;
    }

    public static StartedBoot setup(){
        return new StartedBoot();
    }


    public static void close() throws IOException {
        if (INSTANCE != null) {
            INSTANCE.callClose();
        }
    }

    private void callClose() throws IOException {
        ioProvider.close();
    }


    /**
     * 用一个静态类来实现
     */
    public static class StartedBoot{
        private IoProvider ioProvider;

        public StartedBoot() {
        }
        public StartedBoot ioProvider(IoProvider ioProvider) {
            this.ioProvider = ioProvider;
            return this;
        }

        public IoContext start() {
            INSTANCE = new IoContext(ioProvider);
            return INSTANCE;
        }
    }
}
