package com.example.hiroki.p2p_test;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.hiroki.p2p_test.p2p.AsyncSocket;
import com.example.hiroki.p2p_test.p2p.WiFiDirectBroadcastReceiver;
import com.example.hiroki.p2p_test.util.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class HomeActivity extends AppCompatActivity {
    WiFiDirectBroadcastReceiver mReceiver;
    WifiP2pDevice mTarget;
    AsyncSocket s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        final Logger logger = new Logger("SockActivity", (TextView) HomeActivity.this.findViewById(R.id.textView3));

        final AsyncSocket.SocketListener action = new AsyncSocket.SocketListener() {
            @Override
            public void onSend(boolean result) {
                logger.add("send: " + result);
            }

            @Override
            public void onRecv(byte[] data) {
                logger.add("recv: " + Arrays.toString(data));
            }

            @Override
            public void onClose() {
                logger.add("close socket");
            }
        };

        mReceiver = new WiFiDirectBroadcastReceiver(HomeActivity.this, new WiFiDirectBroadcastReceiver.WfdListener() {
            @Override
            public void onSearch(Collection<WifiP2pDevice> devices) {
                logger.add("wfd.onSearch: " + devices.toString());
                mTarget = null;
                Iterator<WifiP2pDevice> it = devices.iterator();
                if (it.hasNext()) {
                    mTarget = it.next();
                }

                // for develop
                if (devices.isEmpty()) {
                    logger.add("not found peers");
                }
                else {
                    Iterator<WifiP2pDevice> it2 = devices.iterator();
                    while (it2.hasNext()) {
                        WifiP2pDevice dev = it2.next();
                        logger.add("found peers: " + dev.deviceName + " " + WiFiDirectBroadcastReceiver.statusStr(dev.status));
                    }
                }
            }

            @Override
            public void onConnect(AsyncSocket s) {
                logger.add("wfd.onConnect: " + s);
                s.init(action);
                HomeActivity.this.s = s;
            }

            @Override
            public void onDisconnect() {
                logger.add("wfd.onDisconnect");
            }
        }, logger);

        // 検索
        Button searchBtn = (Button) findViewById(R.id.button2);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReceiver.search();
            }
        });

        // 検索と接続のボタンを分離
        // 接続
        Button connectBtn = (Button) findViewById(R.id.button4);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReceiver.connect(mTarget);
            }
        });

        // 適当に通信してみる
        Button sendBtn = (Button) findViewById(R.id.send_button);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (s != null) {
                    byte[] data = {0, 1, 2, 3, 4};
                    s.send(data);
                }
            }
        });

        // 切断
        Button disconnectBtn = (Button) findViewById(R.id.button5);
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReceiver.disconnect();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("stop", "stop2");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("pause", "pause2");
        mReceiver.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("start", "start2");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("restart", "restart2");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("resume", "resume2");
        mReceiver.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("destroy", "destroy2");
        mReceiver = null;
    }
}
