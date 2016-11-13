package com.example.hiroki.p2p_test.lobby;

import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;

import com.example.hiroki.p2p_test.battle.character.Battler;
import com.example.hiroki.p2p_test.battle.controller.ControllerBase;
import com.example.hiroki.p2p_test.battle.controller.SocketController;
import com.example.hiroki.p2p_test.p2p.AsyncSocket;
import com.example.hiroki.p2p_test.p2p.WiFiDirectBroadcastReceiver;

/**
 * Created by hiroki on 2016/11/06.
 */

public class WifiRival extends RivalBase {
    AsyncSocket mRivalSocket;
    WifiP2pDevice mDevice;
    WiFiDirectBroadcastReceiver mReceiver;

    public WifiRival(WifiP2pDevice d, WiFiDirectBroadcastReceiver receiver) {
        super(d.deviceName);
        mDevice = d;
        mReceiver = receiver;
    }

    @Override
    public boolean requestBattle(String name) {
        ConnectionStatus st = getStatus();

        if (st == ConnectionStatus.DISCONNECTED) {
            if (mDevice.status == WifiP2pDevice.CONNECTED) {
                return true;
            }
            mReceiver.connect(mDevice, new WiFiDirectBroadcastReceiver.ConnectListener() {
                @Override
                public void onConnect(AsyncSocket s) {
                    mRivalSocket = s;
                }

                @Override
                public void onDisconnect() {

                }
            });
        }
        return false;
    }

    @Override
    public ControllerBase createController() {
        return new SocketController(mRivalSocket);
    }
}
