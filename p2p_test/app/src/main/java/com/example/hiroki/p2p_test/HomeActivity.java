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

        Button btn = (Button) findViewById(R.id.button2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                mReceiver.search();
                // 接続
                // 適当に通信してみる
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

        if (mReceiver != null) {
            mReceiver.close();
            mReceiver = null;
        }
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("destroy", "destroy2");
    }
}
