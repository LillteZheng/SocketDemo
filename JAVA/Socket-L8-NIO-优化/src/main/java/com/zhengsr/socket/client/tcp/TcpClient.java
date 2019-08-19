package com.zhengsr.socket.client.tcp;

import com.zhengsr.socket.CloseUtils;
import com.zhengsr.socket.client.bean.DeviceInfo;
import com.zhengsr.socket.core.Connector;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class TcpClient extends Connector {
    public TcpClient(SocketChannel socket) throws IOException {
        setup(socket);
    }


    public void exit(){
      CloseUtils.close(this);
    }

    public static TcpClient bindwith(DeviceInfo info){
        try {
            SocketChannel socket = SocketChannel.open();
            socket.connect(new InetSocketAddress(InetAddress.getByName(info.ip),info.port));
            System.out.println("客户端已建立连接");
            System.out.println("客户端信息：" + socket.getLocalAddress().toString());
            System.out.println("服务器信息：" + socket.getRemoteAddress().toString());
            return new TcpClient(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        System.out.println("连接已关闭，无法读取数据!");
    }

    @Override
    protected void onReceiveNewMessage(String str) {
        super.onReceiveNewMessage(str);
        System.out.println("client: "+str);
    }
}
