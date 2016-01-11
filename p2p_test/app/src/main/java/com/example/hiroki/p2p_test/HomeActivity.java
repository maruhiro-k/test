package com.example.hiroki.p2p_test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hiroki.p2p_test.p2p.WiFiDirectBroadcastReceiver;
import com.example.hiroki.p2p_test.p2p.WifiDirectConnector;

public class HomeActivity extends AppCompatActivity {
    WifiDirectConnector cnct;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        cnct = new WifiDirectConnector(getApplicationContext());

        if (!cnct.isEnabled()) {
            Toast.makeText(this, "not psp!", Toast.LENGTH_LONG).show();
        }
        else {
//            Toast.makeText(this, "ok psp!", Toast.LENGTH_LONG).show();
        }

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(getApplicationContext(), getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        Log.d("mManager", mManager.toString());
        Log.d("mChannel", mChannel.toString());
        Log.d("mReceiver", mReceiver.toString());
        Log.d("mIntentFilter", mIntentFilter.toString());

        Button btn = (Button) findViewById(R.id.button2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(HomeActivity.this, BattleActivity.class);
//                startActivity(intent);
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("discover", "success");
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(getApplicationContext(), "fail: " + String.format("%d", reasonCode), Toast.LENGTH_LONG).show();
                    }
                });
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
        unregisterReceiver(mReceiver);
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
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("destroy", "destroy2");
    }
}
