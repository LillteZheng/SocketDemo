package com.zhengsr.nettydemo.chat.server;

import com.sun.deploy.ui.UIFactory;
import com.zhengsr.nettydemo.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * created by @author zhengshaorui on 2019/9/16
 * Describe:
 */
public class ChatServer {
    public static void main(String[] args) throws InterruptedException {
        /**
         * NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器
         * boss 可以理解是 selector 的 accept 单独一个线程
         * worker 可以理解是 selector 的 read 和 write
         */
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
       // ServerBootstrap 是一个启动 NIO 服务的辅助启动类
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    //channel 实例化 NioServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    // 用来处理 handler ，设置连入服务端的 Client 的 SocketChannel 的处理器
                    .childHandler(new ChatServerInitializer())
                    //option 针对NioServerSocketChannel，比如这里 128 个客户端之后，才开始排队
                    .option(ChannelOption.SO_BACKLOG,128)
                    // childOption 针对childHandler 的handler
                    .childOption(ChannelOption.SO_KEEPALIVE,true);

            //这里的启动时异步的，阻塞等待
            ChannelFuture future = b.bind(Constants.PORT).sync();

            future.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()){
                        System.out.println("服务端启动成功");
                    }
                }
            });

            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            future.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
