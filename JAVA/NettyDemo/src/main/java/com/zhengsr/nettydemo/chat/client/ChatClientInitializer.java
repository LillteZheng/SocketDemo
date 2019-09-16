package com.zhengsr.nettydemo.chat.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * created by @author zhengshaorui on 2019/9/16
 * Describe:
 */
public class ChatClientInitializer extends ChannelInitializer<SocketChannel> {
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //采用分隔符处理器，处理黏包问题，防止数据过大收集不到
        pipeline.addLast(new DelimiterBasedFrameDecoder(2048, Delimiters.lineDelimiter()));
        //编码
        pipeline.addLast(new StringDecoder());
        //解码
        pipeline.addLast(new StringEncoder());
        //添加处理器，这里为逻辑的处理
        pipeline.addLast(new ChatClientHandler());
    }
}
