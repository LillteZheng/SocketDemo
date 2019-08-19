package com.zhengsr.socket.core;

import java.io.Closeable;
import java.nio.channels.SocketChannel;
/**
 * created by @author zhengshaorui on 2019/8/15
 * Describe: 提供者，用来注册Socketchannel的输入和输出，并把要实现的
 * 放在 Runnbale 的 run 方法中，供外部实现
 */
public interface IoProvider extends Closeable {

    boolean registerInput(SocketChannel channel,HandleInputCallback callback);
    boolean registerOutput(SocketChannel channel,HandleOutputCallback callback);

    void unRegisterInput(SocketChannel channel);

    void unRegisterOutput(SocketChannel channel);

    abstract class HandleInputCallback implements Runnable{
        @Override
        public void run() {
            canProviderInput();
        }
        protected abstract void canProviderInput();
    }

    abstract class HandleOutputCallback implements Runnable{
        private Object attach;

        @Override
        public void run() {
            canProviderOutput(attach);
        }
        public final void setAttach(Object attach){
            this.attach  =attach;
        }

        public <T> T getAttach(){
            return (T) attach;
        }
        protected abstract void canProviderOutput(Object attach);
    }

}
