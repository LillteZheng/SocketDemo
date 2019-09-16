package com.zhengsr.nettydemo.chat.client;

import com.zhengsr.nettydemo.Constants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * created by @author zhengshaorui on 2019/9/16
 * Describe:
 */
public class ChatClientB {
    public static void main(String[] args) throws InterruptedException, IOException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(bossGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChatClientInitializer());

            //连接服务器
            final ChannelFuture future = bootstrap.connect("localhost", Constants.PORT).sync();

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String msg = br.readLine();
                if (msg.equals("bye")){
                    return;
                }
                future.channel().writeAndFlush(msg+"\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
        }
    }
}
