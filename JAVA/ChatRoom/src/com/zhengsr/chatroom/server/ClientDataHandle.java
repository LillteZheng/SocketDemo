package com.zhengsr.chatroom.server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by zhengshaorui
 * time on 2019/6/26
 */
public class ClientDataHandle {
    private ReaderListener mReaderListener;
    private final writerHandle mWriterHandle;
    private ResponseListener mListener;
    private String mClientInfo;
    public ClientDataHandle(Socket socket,ResponseListener listener) throws IOException {
        mReaderListener = new ReaderListener(socket.getInputStream());
        mWriterHandle = new writerHandle(socket.getOutputStream());
        mReaderListener.start();
        mListener = listener;
        mClientInfo = "Ip: "+socket.getInetAddress() + " port: " + socket.getPort();
        System.out.println("新的客户端连接了: " + mClientInfo);
    }

    public void sendMsg(String msg) {
        mWriterHandle.sendMsg(msg);
    }


    public void exit(){
        mReaderListener.exit();
        mWriterHandle.exit();

    }


    public String getInfo(){
        return mClientInfo;
    }
    public interface ResponseListener{
        void newMsg(ClientDataHandle handle,String msg);
    }

    /**
     * 数据读取监听类
     */
    class ReaderListener extends Thread{
        InputStream inputStream;
        boolean isFinish = false;
        public ReaderListener(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            super.run();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                do {
                    String msg = br.readLine();
                    if (msg != null){
                        //System.out.println("client: "+msg);
                        //把从客户端接收到的数据，传递出去，以便发送给其他客户端
                        if (mListener != null){
                            mListener.newMsg(ClientDataHandle.this,msg);
                        }
                    }else{
                        System.out.println("连接已断开");
                        break;
                    }

                } while (!isFinish);
            } catch (IOException e) {
               // e.printStackTrace();
            }finally {
                exit();
            }
        }

        public void exit(){
            isFinish = true;
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 发送数据
     */
    class writerHandle {
        private PrintStream ps ;
        private ExecutorService executorService ;
        private boolean isFinish = false;
        public writerHandle(OutputStream os) {
            ps = new PrintStream(os);
            executorService = Executors.newSingleThreadExecutor();
        }

        public void exit(){
            isFinish = true;
            ps.close();
            executorService.shutdown();
        }
        public void sendMsg(String msg){
            executorService.execute(new sendSync(msg));
        }

        class sendSync implements Runnable{
            String str;

            public sendSync(String str) {
                this.str = str;
            }

            @Override
            public void run() {
                if (writerHandle.this.isFinish){
                    return;
                }
                ps.println(str);
            }
        }


    }
}
