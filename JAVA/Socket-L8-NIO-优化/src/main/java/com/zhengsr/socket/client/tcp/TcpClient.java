package com.zhengsr.socket.client.tcp;

import com.zhengsr.socket.CloseUtils;
import com.zhengsr.socket.client.bean.DeviceInfo;
import com.zhengsr.socket.core.Connector;
import com.zhengsr.socket.core.packet.ReceivePacket;
import com.zhengsr.socket.core.packet.box.StringReceivePacket;
import com.zhengsr.socket.utils.FileUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class TcpClient extends Connector {
    private final File cachePath;
    public TcpClient(SocketChannel socket,File cachePath) throws IOException {
        this.cachePath = cachePath;
        setup(socket);
    }


    public void exit(){
      CloseUtils.close(this);
    }

    public static TcpClient bindwith(DeviceInfo info,File cachePath){
        try {
            SocketChannel socket = SocketChannel.open();
            socket.connect(new InetSocketAddress(InetAddress.getByName(info.ip),info.port));
            System.out.println("客户端已建立连接");
            System.out.println("客户端信息：" + socket.getLocalAddress().toString());
            System.out.println("服务器信息：" + socket.getRemoteAddress().toString());
            return new TcpClient(socket,cachePath);
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
    protected void onReceivePacket(ReceivePacket packet) {
        super.onReceivePacket(packet);
        if (packet instanceof StringReceivePacket){
            String msg = new String((byte[]) packet.entity());
            System.out.println("client: "+msg);
        }
    }

    @Override
    public File createNewReceiveFile() {
        return FileUtils.createRandomTemp(cachePath);
    }
}
