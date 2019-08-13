package com.zhengsr.socket_6.client;

import com.zhengsr.socket_6.client.bean.DeviceInfo;
import com.zhengsr.socket_6.client.tcp.TcpClient;
import com.zhengsr.socket_6.client.udp.UdpSearch;

public class Client {
    public static void main(String[] args) {
        DeviceInfo info = UdpSearch.searchServer(4);
            System.out.println("info: " + info);
        if (info != null) {
            TcpClient.bindwith(info);
        }
    }
}
