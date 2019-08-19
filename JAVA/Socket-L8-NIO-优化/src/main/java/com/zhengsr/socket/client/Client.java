package com.zhengsr.socket.client;

import com.zhengsr.socket.client.bean.DeviceInfo;
import com.zhengsr.socket.client.tcp.TcpClient;
import com.zhengsr.socket.client.udp.UdpSearch;
import com.zhengsr.socket.core.IoContext;
import com.zhengsr.socket.core.impl.IoSelectorProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Client {
    public static void main(String[] args) throws IOException {
        IoContext.setup()
                .ioProvider(new IoSelectorProvider())
                .start();
        DeviceInfo info = UdpSearch.searchServer(10000);
        System.out.println("Server:" + info);

        if (info != null) {
            TcpClient tcpClient = null;

            try {
                tcpClient = tcpClient.bindwith(info);
                if (tcpClient == null) {
                    return;
                }

                write(tcpClient);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (tcpClient != null) {
                    tcpClient.exit();
                }
            }
        }
        IoContext.close();
    }

    private static void write(TcpClient tcpClient) throws IOException {
        // 构建键盘输入流
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        do {
            // 键盘读取一行
            String str = input.readLine();
            // 发送到服务器
            tcpClient.sendMsg(str);
            tcpClient.sendMsg(str);
            tcpClient.sendMsg(str);
            tcpClient.sendMsg(str);

            if ("00bye00".equalsIgnoreCase(str)) {
                break;
            }
        } while (true);
    }
}
