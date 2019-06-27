package com.zhengsr.chatroom;
/**
 * created by zhengshaorui
 * time on 2019/6/26
 */
public class DeviceInfo {
    public String ip;
    public int port;
    public String data;

    public DeviceInfo(String ip, int port, String data) {
        this.ip = ip;
        this.port = port;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Device{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", data='" + data + '\'' +
                '}';
    }
}
