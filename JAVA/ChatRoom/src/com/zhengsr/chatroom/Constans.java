package com.zhengsr.chatroom;

public class Constans {
    public static int PORT = 9090;
    public static int TCP_PORT = 50000;
    public static int BROADCAST_PORT = 8989;
    public static int TIME_OUT = 3000;
    public static String BROADCAST_IP = "255.255.255.255";
    public static String SN_HEADER = "请回端口:";

    public static int CMD_BROAD = 0x1001;
    public static int CMD_BRO_RESPONSE = 0x1001;


    public static String createSn(int port){
        return SN_HEADER+port;
    }
    public static int parsePort(String msg){
        if (msg.startsWith(SN_HEADER)) {
            return Integer.parseInt(msg.substring(SN_HEADER.length()));
        }
        return -1;
    }
}
