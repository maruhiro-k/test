package com.example.hiroki.p2p_test.lobby.rival;

import android.net.wifi.p2p.WifiP2pDevice;

import com.example.hiroki.p2p_test.battle.controller.ControllerBase;
import com.example.hiroki.p2p_test.battle.controller.SocketController;
import com.example.hiroki.p2p_test.lobby.p2p.AuraBattleProtocol;
import com.example.hiroki.p2p_test.lobby.p2p.AsyncSocket;
import com.example.hiroki.p2p_test.lobby.p2p.WiFiDirectBroadcastReceiver;

/**
 * Created by hiroki on 2016/11/06.
 */

public class WifiRival extends RivalBase {
    private AsyncSocket mRivalSocket;
    private WifiP2pDevice mDevice;
    private WiFiDirectBroadcastReceiver mReceiver;
    private String mName;

    public WifiRival(WifiP2pDevice d, WiFiDirectBroadcastReceiver receiver) {
        super(d.deviceName);
        mDevice = d;
        mReceiver = receiver;
    }

    @Override
    public String getBattlerName() {
        return mName;
    }

    @Override
    public boolean requestBattle(final String name) {
        if (mRivalSocket != null && mRivalSocket.isConnected()) {
            return true;    // すでにソケットがつながっている
        }

        setStatus(ConnectionStatus.DISCONNECTED);

        // ソケットがまだつながっていないならつなぐ
        mReceiver.connect(mDevice, new WiFiDirectBroadcastReceiver.ConnectListener() {
            @Override
            public void onConnect(AsyncSocket s) {
                mRivalSocket = s;
                setStatus(ConnectionStatus.CONNECTED);
                requestBattleImpl(name);
            }

            @Override
            public void onDisconnect() {
                setStatus(ConnectionStatus.DISCONNECTED);
            }
        });
        return true;
    }

    // @Override
    public void cancelBattle() {
        requestBattleImpl(null);
    }

    private void requestBattleImpl(String name) {
        if (name != null) {
            setStatus(ConnectionStatus.SEND_REQUEST);
        }
        else {
            setStatus(ConnectionStatus.CONNECTED);
        }

        mReceiver.shakeHand(mRivalSocket, AuraBattleProtocol.APP_MAGIC, name, new WiFiDirectBroadcastReceiver.ShakeListener() {
            @Override
            public void onRecv(final String rivalName) {
                if (mListener != null) {
                    if (rivalName != null) {
                        mName = rivalName;
                        setStatus(ConnectionStatus.ALL_OK);
                    }
                    else {
                        setStatus(ConnectionStatus.CONNECTED);
                    }
                }
            }
        });
    }

    @Override
    public ControllerBase createController() {
        return new SocketController(mRivalSocket);
    }
}
