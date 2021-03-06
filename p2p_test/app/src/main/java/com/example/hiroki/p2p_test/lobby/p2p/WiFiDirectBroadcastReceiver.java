package com.example.hiroki.p2p_test.lobby.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Collection;

import static com.example.hiroki.p2p_test.lobby.p2p.WifiSocketProtocol.SOCK_PORT;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    IntentFilter mIntentFilter;
    Context mContext;
    SearchListener mSearchListener;
    ConnectListener mConnectListener;
    ServerSocket ss;

    public WiFiDirectBroadcastReceiver(Context c, SearchListener listener) {
        super();

        mContext = c;
        mSearchListener = listener;
        mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, mContext.getMainLooper(), null);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    public boolean isEnabled() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT);
    }

    public boolean start() {
        if (!isEnabled()) {
            return false;
        }

        mContext.registerReceiver(this, mIntentFilter);
        return true;
    }

    public void close() {
        mContext.unregisterReceiver(this);
    }

    public void search() {
        if (!isEnabled()) {
            return;
        }

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reasonCode) {
            }
        });
    }

    public boolean connect(WifiP2pDevice target, ConnectListener listener) {
        if (!isEnabled()) {
            Log.d("A", "A2");
            return false;
        }
        if (target == null) {
            Log.d("A", "A3");
            return false;
        }
        if (target.deviceAddress.isEmpty()) {
            return false;
        }

        mConnectListener = listener;

        // すでにつながってる可能性もある
        if (target.status == WifiP2pDevice.CONNECTED) {
            Log.d("A", "A4");
            // ソケットつなぐだけ
            connectSocket();
            return true;
        }

        // つながっていなかったらconnect
        Log.d("A", "A5");
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = target.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        Log.d("A", "A6");
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d("A", "A7");
                // ソケットつなぐ？ここでやる？
                connectSocket();
            }

            @Override
            public void onFailure(int reason) {
                Log.d("A", "A8");
            }
        });
        Log.d("A", "A9");
        return true;
    }

    protected void connectSocket() {
        mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if (info.groupOwnerAddress == null) {
                    return;
                }
                if (info.isGroupOwner) {
                    connectServerSocket();
                } else {
                    connectClientSocket(info.groupOwnerAddress.getHostAddress());
                }
            }
        });
    }

    protected void connectServerSocket() {
        Log.d("server", "connect start!");
        ss = new ServerSocket();
        ss.accept(SOCK_PORT, new ServerSocket.AcceptListener() {
            @Override
            public void onAccept(AsyncSocket s) {
                Toast.makeText(mContext, "onAccept:"+s, Toast.LENGTH_LONG).show();
                if (mConnectListener != null) {
                    mConnectListener.onConnect(s);
                }
            }

            @Override
            public void test(String e) {
                Toast.makeText(mContext, "onAccept:Error:"+e, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void connectClientSocket(String address) {
        Log.d("client", "connect start!");
        final ClientSocket cs = new ClientSocket();
        cs.connect(address, SOCK_PORT, new ClientSocket.ConnectListener() {
            @Override
            public void onConnect(boolean result) {
                if (mConnectListener != null) {
                    if (result) {
                        mConnectListener.onConnect(cs);
                    }
                    else {
                        mConnectListener.onDisconnect();
                    }
                }
            }
        });
    }

    public void disconnect() {
        if (ss != null) {
            ss.close();
            ss = null;
        }
        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });

        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

    public void shakeHand(AsyncSocket target, String app_name, String player_name, final ShakeListener shakeListener) {
        if (target == null) {
            shakeListener.onRecv(null); // ソケットがないので拒絶扱い
            return;
        }

        WifiSocketProtocol wsp = new WifiSocketProtocol(target, app_name + "_REQ");

        wsp.setListener(new AsyncSocket.SocketListener() {
            @Override
            public void onSend(boolean result) {
                Log.d("grea", "sendShakeResult: " + result);
                if (! result) {
                    shakeListener.onRecv(null); // 失敗なら拒絶扱い
                }
            }

            @Override
            public void onRecv(byte[] data) {
                Log.d("grea", "recvShakeResult: " + data);
                if (data.length == 1 && data[0] == 0) {
                    shakeListener.onRecv(null); // 0なら拒絶
                }
                else {
                    shakeListener.onRecv(new String(data));
                }
            }

            @Override
            public void onClose() {
                shakeListener.onRecv(null); // 切断なら拒絶扱い
            }
        });

        if (player_name != null) {
            wsp.send(player_name.getBytes());
        }
        else {
            byte[] data = {0};
            wsp.send(data);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
            } else {
                // Wi-Fi P2P is not enabled
            }

        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            if (mManager != null) {
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        Collection<WifiP2pDevice> decices = peers.getDeviceList();
                        if (mSearchListener != null) {
                            mSearchListener.onSearch(decices);
                        }
                    }
                });
            }
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && networkInfo.isConnected()) {
                // つなぎに来た場合
                // なにもしなくていいのかな
                WifiP2pInfo p2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                if (p2pInfo != null && p2pInfo.isGroupOwner) {
                    connectServerSocket();
                }
            }
            else {
                // 切断された場合
                if (mConnectListener != null) {
                    mConnectListener.onDisconnect();
                }
            }
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    static public String errStr(int code) {
        if (code == WifiP2pManager.P2P_UNSUPPORTED)
            return "[P2P_UNSUPPORTED]";
        else if (code == WifiP2pManager.ERROR)
            return "[ERROR]";
        else if (code == WifiP2pManager.BUSY)
            return "[BUSY]";
        else
            return "[unknown]";
    }

    static public String statusStr(int status) {
        if (status == WifiP2pDevice.CONNECTED) {
            return "[CONNECTED]";
        } else if (status == WifiP2pDevice.INVITED) {
            return "[INVITED]";
        } else if (status == WifiP2pDevice.FAILED) {
            return "[FAILED]";
        } else if (status == WifiP2pDevice.AVAILABLE) {
            return "[AVAILABLE]";
        } else if (status == WifiP2pDevice.UNAVAILABLE) {
            return "[UNAVAILABLE]";
        } else {
            return "[unknown]";
        }
    }

    public interface SearchListener {
        void onSearch(Collection<WifiP2pDevice> devices);
    }
    public interface ConnectListener {
        void onConnect(AsyncSocket s);
        void onDisconnect();
    }
    public interface ShakeListener {
        void onRecv(final String rivalName);
    }
}
