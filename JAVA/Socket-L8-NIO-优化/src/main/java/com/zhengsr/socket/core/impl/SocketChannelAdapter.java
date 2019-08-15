package com.zhengsr.socket.core.impl;

import com.zhengsr.socket.CloseUtils;
import com.zhengsr.socket.core.IoArgs;
import com.zhengsr.socket.core.IoProvider;
import com.zhengsr.socket.core.Receiver;
import com.zhengsr.socket.core.Sender;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 * created by @author zhengshaorui on 2019/8/15
 * Describe: SocketChannel的具体实现类，socketchannel 传递进来之后，
 * 把读写分发给 ioprovider
 */
public class SocketChannelAdapter implements Sender, Receiver, Closeable {
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private SocketChannel channel;
    private OnChannelStatusChangedListener listener;
    private IoProvider ioProvider;

    private IoArgs.IoArgsEventListener receiverEventListener;
    private IoArgs.IoArgsEventListener senderEventListener;


    public SocketChannelAdapter(SocketChannel channel, OnChannelStatusChangedListener listener, IoProvider ioProvider) throws IOException {
        //配置成非阻塞模式
        channel.configureBlocking(false);
        this.channel = channel;
        this.listener = listener;
        this.ioProvider = ioProvider;

    }

    @Override
    public boolean receiverAsync(IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()){
            throw new IOException("Current channel is closed!");
        }
        receiverEventListener = listener;
        //读注册给 ioprovider，让它完成具体的实现
        return ioProvider.registerInput(channel,inputCallback);
    }

    @Override
    public boolean sendAsync(IoArgs args, IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()){
            throw new IOException("Current channel is closed!");
        }
        senderEventListener = listener;
        return ioProvider.registerOutput(channel,outputCallback);
    }

    @Override
    public void close()  {
        if (isClosed.compareAndSet(false,true)){
            //解除注册
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterOutput(channel);
            //关闭
            CloseUtils.close(channel);
            //回调当前channel已关闭
            listener.onChannelClosed(channel);
        }
    }

    private final IoProvider.HandleInputCallback inputCallback = new IoProvider.HandleInputCallback() {
        @Override
        protected void canProviderInput() {
            if (isClosed.get()){
                return;
            }
            IoArgs args = new IoArgs();
            IoArgs.IoArgsEventListener listener = SocketChannelAdapter.this.receiverEventListener;
            if (listener != null){
                listener.onStart(args);
            }

            try {
                int read = args.read(channel);
                if (read > 0 && listener != null){
                    listener.onCompleted(args);
                }else{
                    throw new IOException("Cannot read any data!");
                }
            } catch (IOException e) {
                //e.printStackTrace();
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };

    private final IoProvider.HandleOutputCallback outputCallback = new IoProvider.HandleOutputCallback() {
        @Override
        protected void canProviderOutput(Object attach) {
            if (isClosed.get()){
                return;
            }
        }
    };


    /**
     * 是否异常关闭
     */
    public interface OnChannelStatusChangedListener {
        void onChannelClosed(SocketChannel channel);
    }
}
