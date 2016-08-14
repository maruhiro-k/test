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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    IntentFilter mIntentFilter;
    Context mContext;
    WfdListener mListener;
    Logger mLogger;
    ServerSocket ss;

    public interface LogAction {
        public abstract void add(String log);
    }

    public WiFiDirectBroadcastReceiver(Context c, WfdListener listener, Logger logger) {
        super();

        mContext = c;
        mListener = listener;
        mLogger = logger;
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
            //mLogger.add("P2P Disabled! (search)");
            return;
        }
        //mLogger.add("begin discover");

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //mLogger.add("discover: success");
            }

            @Override
            public void onFailure(int reasonCode) {
                //mLogger.add("discover: fail: " + errStr(reasonCode));
            }
        });
    }

    public void connect(WifiP2pDevice target) {
        Log.d("A", "A1");
        if (!isEnabled()) {
            Log.d("A", "A2");
            //mLogger.add("P2P Disabled! (connect)");
            return;
        }
        if (target == null) {
            Log.d("A", "A3");
            //mLogger.add("hostName is invalid");
            return;
        }

        // すでにつながってる可能性もある
        //mLogger.add("target status: " + statusStr(target.status));
        if (target.status == WifiP2pDevice.CONNECTED) {
            Log.d("A", "A4");
            // ソケットつなぐだけ
            connectSocket();
            return;
        }

        // つながっていなかったらconnect
        Log.d("A", "A5");
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = target.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        //mLogger.add("begin connect: " + target.deviceName);

        Log.d("A", "A6");
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d("A", "A7");
                //success logic
                //mLogger.add("connect success");
                // ソケットつなぐ？ここでやる？
                connectSocket();
            }

            @Override
            public void onFailure(int reason) {
                Log.d("A", "A8");
                //failure logic
                //mLogger.add("connect: fail: " + errStr(reason));
            }
        });
        Log.d("A", "A9");
    }

    protected void connectSocket() {
        mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if (info.groupOwnerAddress == null) {
                    //mLogger.add("info.groupOwnerAddress is null");
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
        ss.addLogger(mLogger);
        ss.accept(12345, new ServerSocket.AcceptListener() {
            @Override
            public void onAccept(AsyncSocket s) {
                //mLogger.add("accept: " + s);
                if (mListener != null) {
                    mListener.onConnect(s);
                }
            }
        });
    }

    protected void connectClientSocket(String address) {
        final ClientSocket cs = new ClientSocket();
        cs.addLogger(mLogger);
        cs.connect(address, 12345, new ClientSocket.ConnectListener() {
            @Override
            public void onConnect(boolean result) {
                //mLogger.add("connect: " + result);
                if (mListener != null) {
                    mListener.onConnect(cs);
                }
            }
        });
    }

    public void disconnect() {
        //mLogger.add("begin disconnect");

        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //mLogger.add("cancelConnect success");
            }

            @Override
            public void onFailure(int reason) {
                //mLogger.add("cancelConnect failed: " + errStr(reason));
            }
        });

        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //mLogger.add("removeGroup success");
            }

            @Override
            public void onFailure(int reason) {
                //mLogger.add("removeGroup failed: " + errStr(reason));
            }
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            //mLogger.add("WIFI_STATE_CHANGED_ACTION");

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                //mLogger.add("check: P2P enabled!");
            } else {
                // Wi-Fi P2P is not enabled
                //mLogger.add("Error: p2p disabled!");
            }

        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //mLogger.add("WIFI_PEERS_CHANGED_ACTION");

            // Call WifiP2pManager.requestPeers() to get a list of current peers
            if (mManager != null) {
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        Collection<WifiP2pDevice> decices = peers.getDeviceList();
                        if (mListener != null) {
                            mListener.onSearch(decices);
                        }
                    }
                });
            }
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            //mLogger.add("WIFI_CONNECTION_CHANGED_ACTION");
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
                if (mListener != null) {
                    mListener.onDisconnect();
                }
            }
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            //mLogger.add("WIFI_THIS_DEVICE_CHANGED_ACTION");
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

    public interface WfdListener {
        void onSearch(Collection<WifiP2pDevice> devices);
        void onConnect(AsyncSocket s);
        void onDisconnect();
    }
}
