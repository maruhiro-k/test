package com.example.hiroki.p2p_test.p2p;

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

import com.example.hiroki.p2p_test.util.Logger;

import java.util.Collection;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    IntentFilter mIntentFilter;
    Context mContext;
    SearchListener mSearchListener;
    ConnectListener mConnectListener;
    ServerSocket ss;

    public interface LogAction {
        public abstract void add(String log);
    }

    public WiFiDirectBroadcastReceiver(Context c, SearchListener listener, Logger logger) {
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
        if (target.deviceAddress == "") {
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
        ss = new ServerSocket();
        ss.accept(12345, new ServerSocket.AcceptListener() {
            @Override
            public void onAccept(AsyncSocket s) {
                if (mConnectListener != null) {
                    mConnectListener.onConnect(s);
                }
            }
        });
    }

    protected void connectClientSocket(String address) {
        final ClientSocket cs = new ClientSocket();
        cs.connect(address, 12345, new ClientSocket.ConnectListener() {
            @Override
            public void onConnect(boolean result) {
                if (mConnectListener != null) {
                    mConnectListener.onConnect(cs);
                }
            }
        });
    }

    public void disconnect() {
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

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && networkInfo.isConnected()) {
                // つなぎに来た場合
                // なにもしなくていいのかな
                WifiP2pInfo p2pInfo = (WifiP2pInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
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
}
