package com.zhengsr.socket.client;

import com.zhengsr.socket.client.bean.DeviceInfo;
import com.zhengsr.socket.client.tcp.TcpClient;
import com.zhengsr.socket.client.udp.UdpSearch;
import com.zhengsr.socket.core.IoContext;
import com.zhengsr.socket.core.impl.IoSelectorProvider;
import com.zhengsr.socket.core.packet.box.FileSendPacket;
import com.zhengsr.socket.utils.FileUtils;

import java.io.*;

public class Client {
    public static void main(String[] args) throws IOException {
        File cachePath = FileUtils.getCacheDir("client");
        IoContext.setup()
                .ioProvider(new IoSelectorProvider())
                .start();
        DeviceInfo info = UdpSearch.searchServer(10000);
        System.out.println("Server:" + info);

        if (info != null) {
            TcpClient tcpClient = null;

            try {
                tcpClient = tcpClient.bindwith(info,cachePath);
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
            if ("00bye00".equalsIgnoreCase(str)) {
                break;
            }
            if (str.startsWith("--f")){
                String[] msg = str.split(" ");
                if (msg.length > 1){
                    String path = msg[1];
                    File file = new File(path);
                    if (file.isFile()) {
                        FileSendPacket packet = new FileSendPacket(file);
                        tcpClient.sendPacket(packet);
                        continue;
                    }
                }
            }
            // 发送到服务器
            tcpClient.sendMsg(str);



        } while (true);
    }
}
