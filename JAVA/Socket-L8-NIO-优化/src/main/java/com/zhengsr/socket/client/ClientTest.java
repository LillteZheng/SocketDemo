package com.zhengsr.socket.client;

import com.zhengsr.socket.client.bean.DeviceInfo;
import com.zhengsr.socket.client.tcp.TcpClient;
import com.zhengsr.socket.client.udp.UdpSearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * created by @author zhengshaorui on 2019/8/15
 * Describe: 压力测试类
 */
public class ClientTest {
    private static boolean done;

    public static void main(String[] args) throws IOException {
        DeviceInfo info = UdpSearch.searchServer(10000);
        System.out.println("Server:" + info);
        if (info == null) {
            return;
        }

        // 当前连接数量
        int size = 0;
        final List<TcpClient> tcpClients = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            try {
                TcpClient tcpClient = TcpClient.bindwith(info);
                if (tcpClient == null) {
                    System.out.println("连接异常");
                    continue;
                }

                tcpClients.add(tcpClient);

                System.out.println("连接成功：" + (++size));

            } catch (Exception e) {
                System.out.println("连接异常");
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        System.in.read();
        System.out.println("什么");
        Runnable runnable = () -> {
            while (!done) {
                for (TcpClient tcpClient : tcpClients) {
                    System.out.println("发送");
                    tcpClient.send("Hello~~");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

        System.in.read();

        // 等待线程完成
        done = true;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 客户端结束操作
        for (TcpClient tcpClient : tcpClients) {
            tcpClient.exit();
        }

    }


}
