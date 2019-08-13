package com.zhengsr.socket_6;

import java.io.Closeable;
import java.io.IOException;

public class CloseUtils  {

    public static void close(Closeable... closeables){
        if (closeables != null){
            for (Closeable closeable : closeables) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
