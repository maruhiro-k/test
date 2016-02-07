package com.example.hiroki.p2p_test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hiroki.p2p_test.p2p.WiFiDirectBroadcastReceiver;

public class HomeActivity extends AppCompatActivity {
    WiFiDirectBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mReceiver = new WiFiDirectBroadcastReceiver(HomeActivity.this, new WiFiDirectBroadcastReceiver.LogAction() {
            int num = 0;

            @Override
            public void add(String log) {
                Log.d("HomeActivity", log);
                TextView t = (TextView) HomeActivity.this.findViewById(R.id.textView3);
                t.setMovementMethod(ScrollingMovementMethod.getInstance());
                t.append(++num + ": " + log + "\n");
            }
        });

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
                mReceiver.connect();
            }
        });

        // 適当に通信してみる

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