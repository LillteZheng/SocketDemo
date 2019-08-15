package com.zhengsr.socket.client.bean;

public class DeviceInfo {
    public String ip;
    public int port;
    public String info;


    public DeviceInfo(String ip, int port, String info) {
        this.ip = ip;
        this.port = port;
        this.info = info;
    }


    @Override
    public String toString() {
        return "DeviceInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", info='" + info + '\'' +
                '}';
    }
}
