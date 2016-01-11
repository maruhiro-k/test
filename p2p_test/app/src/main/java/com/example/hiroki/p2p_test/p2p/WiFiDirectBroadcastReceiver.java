package com.example.hiroki.p2p_test.p2p;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Collection;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    Activity activity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, Activity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                Log.d("check", "P2P enabled!");
            } else {
                // Wi-Fi P2P is not enabled
                Toast.makeText(context, "Error: p2p disabled!", Toast.LENGTH_LONG).show();
            }

        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            if (manager != null) {
                manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        Log.d("found peers", "test" + peers.toString());
                        Toast.makeText(activity,  peers.toString(), Toast.LENGTH_LONG).show();

                        Collection<WifiP2pDevice> decices = peers.getDeviceList();
                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = decices.iterator().next().deviceAddress;
                        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                //success logic
                                Log.d("connect", "suc");
                            }

                            @Override
                            public void onFailure(int reason) {
                                //failure logic
                                Log.d("connect", "fail: " + String.format("%d", reason));
                            }
                        });

                    }
                });
            }
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }
}
