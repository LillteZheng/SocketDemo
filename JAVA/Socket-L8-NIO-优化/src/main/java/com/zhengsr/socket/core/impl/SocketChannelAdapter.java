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

    private IoArgs.IoArgsEventProcessor receiverEventListener;
    private IoArgs.IoArgsEventProcessor senderEventListener;



    public SocketChannelAdapter(SocketChannel channel, OnChannelStatusChangedListener listener,
                                IoProvider ioProvider) throws IOException {
        //配置成非阻塞模式
        channel.configureBlocking(false);
        this.channel = channel;
        this.listener = listener;
        this.ioProvider = ioProvider;

    }
 @Override
    public void setReceiveListener(IoArgs.IoArgsEventProcessor processor) throws IOException {

        receiverEventListener = processor;
    }

    @Override
    public boolean postReceiveAsync() throws IOException {
        if (isClosed.get()){
            throw new IOException("Current channel is closed!");
        }
        //读注册给 ioprovider，让它完成具体的实现
        return ioProvider.registerInput(channel,inputCallback);
    }

    @Override
    public void setSendListener(IoArgs.IoArgsEventProcessor processor) throws IOException {
        senderEventListener = processor;
    }

    @Override
    public boolean postSendAsync() throws IOException {
        if (isClosed.get()){
            throw new IOException("Current channel is closed!");
        }
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
            IoArgs.IoArgsEventProcessor listener = SocketChannelAdapter.this.receiverEventListener;
            IoArgs args = listener.provideIoArgs();
            try {
                int read = args.readFrom(channel);
                if (read > 0){
                    listener.onConsumeCompleted(args);
                }else{
                    listener.onConsumeFailed(args,new IOException("Cannot readFrom any data!"));
                }
            } catch (IOException e) {
                //e.printStackTrace();
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };

    private final IoProvider.HandleOutputCallback outputCallback = new IoProvider.HandleOutputCallback() {
        @Override
        protected void canProviderOutput() {
            if (isClosed.get()){
                return;
            }
            IoArgs.IoArgsEventProcessor listener = senderEventListener;
            IoArgs args = listener.provideIoArgs();


            try {
                // 具体的读取操作
                if (args.writeTo(channel) > 0) {
                    // 读取完成回调
                    listener.onConsumeCompleted(args);
                } else {
                    listener.onConsumeFailed(args,new IOException("Cannot write any data!"));
                }
            } catch (IOException ignored) {
                CloseUtils.close(SocketChannelAdapter.this);
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
