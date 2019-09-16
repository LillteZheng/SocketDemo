package com.zhengsr.nettydemo.chat.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * created by @author zhengshaorui on 2019/9/16
 * Describe:
 */
public class ChatClientHandler extends SimpleChannelInboundHandler<String> {

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        //收到服务端消息
        System.out.println(s);
    }
}
