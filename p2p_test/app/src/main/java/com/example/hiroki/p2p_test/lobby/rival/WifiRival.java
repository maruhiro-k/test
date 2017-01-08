package com.example.hiroki.p2p_test.lobby.rival;

import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;

import com.example.hiroki.p2p_test.battle.controller.ControllerBase;
import com.example.hiroki.p2p_test.battle.controller.SocketController;
import com.example.hiroki.p2p_test.lobby.p2p.AsyncSocket;
import com.example.hiroki.p2p_test.lobby.p2p.AuraBattleProtocol;
import com.example.hiroki.p2p_test.lobby.p2p.WiFiDirectBroadcastReceiver;

/**
 * Created by hiroki on 2016/11/06.
 */

public class WifiRival extends RivalBase {
    private AsyncSocket mRivalSocket;
    private WifiP2pDevice mDevice;
    private WiFiDirectBroadcastReceiver mReceiver;
    private String mName;
    private String mSendName;

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
        mListener.test("requestBattle:"+name+"+"+getStatus());
        if (getStatus() == ConnectionStatus.DISCONNECTED) {
            // ソケットがまだつながっていないならつなぐ
            mReceiver.connect(mDevice, new WiFiDirectBroadcastReceiver.ConnectListener() {
                @Override
                public void onConnect(AsyncSocket s) {
                    mRivalSocket = s;
                    if (s.s == null) {
                        mListener.test("requestBattle:onConnect:" + s.s + "+" + getStatus());
                    }
                    requestBattleImpl(name);
                }

                @Override
                public void onDisconnect() {
                    cancelBattle();
                }
            });
        }
        else {
            // すでにつながっているなら、リクエスト再送
            mListener.test("requestBattle:connected:"+name+"+"+getStatus());
            requestBattleImpl(name);
        }
        return true;
    }

    // @Override
    public void cancelBattle() {
        if (mRivalSocket != null) {
            requestBattleImpl(null);    // キャンセルしたことを相手に送る
            mRivalSocket.close();
        }
        mRivalSocket = null;
        mName = null;
        mSendName = null;
        dispatchBattleStatus();
    }

    @Override
    ConnectionStatus getStatus() {
        if (mDevice.status != WifiP2pDevice.CONNECTED) {
            return ConnectionStatus.DISCONNECTED;
        }
        else if (mRivalSocket == null || ! mRivalSocket.isConnected()) {
            return ConnectionStatus.DISCONNECTED;
        }
        else if (mSendName == null) {
            return ConnectionStatus.CONNECTED;
        }
        else if (mName == null) {
            return ConnectionStatus.SEND_REQUEST;
        }
        else {
            return ConnectionStatus.ALL_OK;
        }
    }

    private void requestBattleImpl(String name) {
        mSendName = name;
        if (name != null) {
            Log.d("grea", "shakeHand : " + name);
            mListener.test("send");
        }

        mReceiver.shakeHand(mRivalSocket, AuraBattleProtocol.APP_MAGIC, name, new WiFiDirectBroadcastReceiver.ShakeListener() {
            @Override
            public void onRecv(final String rivalName) {
                mName = rivalName;
                if (rivalName != null) {
                    Log.d("grea", "recvShake : " + rivalName);
                    mListener.test("recv+"+getStatus());
                }
                if (getStatus() != ConnectionStatus.DISCONNECTED) {
                    dispatchBattleStatus();
                }
            }
        });
    }

    @Override
    public ControllerBase createController() {
        return new SocketController(mRivalSocket);
    }
}
