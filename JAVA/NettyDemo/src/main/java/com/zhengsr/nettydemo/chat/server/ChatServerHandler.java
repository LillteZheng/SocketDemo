package com.zhengsr.nettydemo.chat.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * created by @author zhengshaorui on 2019/9/16
 * Describe:
 */
public class ChatServerHandler extends SimpleChannelInboundHandler<String> {
    //单例
    static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        //提示其他客户端，有新客户端加入
        group.writeAndFlush("SERVER - "+channel.remoteAddress()+"加入群聊\n");
        group.add(channel);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        //提示其他客户端，有新客户端加入
        System.out.println("handlerRemoved");
        group.writeAndFlush("SERVER - "+channel.remoteAddress()+"离开\n");
      //  group.remove(channel);
        // A closed Channel is automatically removed from ChannelGroup,
        // so there is no need to do "channels.remove(ctx.channel());"
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        Channel clientChannel = channelHandlerContext.channel();
        //打印信息
        for (Channel channel : group) {
            if (channel != clientChannel){
                channel.writeAndFlush("[" + clientChannel.remoteAddress() + "]" + s + "\n");
            }else{
                channel.writeAndFlush("[you]" + s + "\n");
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelInactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:"+incoming.remoteAddress()+"异常");
        cause.printStackTrace();
        ctx.close();
    }
}
