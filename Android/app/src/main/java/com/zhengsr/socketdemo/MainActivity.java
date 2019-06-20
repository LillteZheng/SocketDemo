package com.zhengsr.socketdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private UdpBroServer mUdpBroServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUdpBroServer = new UdpBroServer();
        mUdpBroServer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUdpBroServer.exit();
    }
}
