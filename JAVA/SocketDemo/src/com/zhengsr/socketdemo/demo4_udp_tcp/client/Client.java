package com.zhengsr.socketdemo.demo4_udp_tcp.client;

import com.zhengsr.socketdemo.demo4_udp_tcp.DeviceInfo;
/**
 * created by zhengshaorui
 * time on 2019/6/26
 */
public class Client {
    public static void main(String[] args) {
        DeviceInfo device = UdpSearch.start(1);
        System.out.println("device: "+device.toString());

        //开始于 tcp 服务端通信
        if (device != null) {
            TcpClient.bindWith(device);
        }

    }
}
