package com.example.hiroki.p2p_test.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    IntentFilter mIntentFilter;
    Context mContext;
    Collection<WifiP2pDevice> mDecices;
    LogAction mLogger;
    ServerSocket ss;
    ClientSocket cs;


    public interface LogAction {
        public abstract void add(String log);
    }

    public WiFiDirectBroadcastReceiver(Context c, LogAction logger) {
        super();

        mContext = c;
        mLogger = logger;
        mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, mContext.getMainLooper(), null);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        ss = new ServerSocket();
        ss.execute();
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
        if (ss != null) {
            ss.close();
        }
    }

    public void search() {
        if (!isEnabled()) {
            mLogger.add("P2P Disabled! (search)");
            return;
        }
        mLogger.add("begin discover");

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mLogger.add("discover: success");
            }

            @Override
            public void onFailure(int reasonCode) {
                mLogger.add("discover: fail: " + errStr(reasonCode));
            }
        });
    }

    public void connect() {
        if (!isEnabled()) {
            mLogger.add("P2P Disabled! (connect)");
            return;
        }
        if (mDecices.isEmpty()) {
            mLogger.add("device is nothing");
            return;
        }

        final WifiP2pDevice dev = mDecices.iterator().next();

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = dev.deviceAddress;

        mLogger.add("begin connect: " + dev.deviceName);
        Log.d("test", dev.toString());
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
                mLogger.add("connect success");
                /*
                if (ss != null) {
                    ss.close(new SocketBase.CloseListener() {
                        @Override
                        public void onClosed(SocketBase s) {
                            cs = new ClientSocket();
                            cs.execute(dev.deviceAddress);
                        }
                    });
                }
                */
                mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        mLogger.add(info.toString());
                        final InetAddress address = info.groupOwnerAddress;
                        //socket communication
                        if (ss != null) {
                            ss.close(new SocketBase.CloseListener() {
                                @Override
                                public void onClosed(SocketBase s) {
                                    cs = new ClientSocket();
                                    cs.execute(address);
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                //failure logic
                mLogger.add("connect: fail: " + errStr(reason));
            }
        });

        // connectした側がクライアントになるのか
        //
    }

    public void disconnect() {
        mLogger.add("begin disconnect");
        mLogger.add("not implemented!");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        mLogger.add("action: " + action);

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            mLogger.add("WIFI_STATE_CHANGED_ACTION");

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                mLogger.add("check: P2P enabled!");
            } else {
                // Wi-Fi P2P is not enabled
                mLogger.add("Error: p2p disabled!");
            }

        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            mLogger.add("WIFI_PEERS_CHANGED_ACTION");

            // Call WifiP2pManager.requestPeers() to get a list of current peers
            if (mManager != null) {
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        mDecices = peers.getDeviceList();
                        if (mDecices.isEmpty()) {
                            mLogger.add("not found peers");
                            return;
                        }

                        Iterator<WifiP2pDevice> it = mDecices.iterator();
                        while (it.hasNext()) {
                            WifiP2pDevice dev = it.next();
                            mLogger.add("found peers: " + dev.deviceName);
                        }
                    }
                });

                /*
                mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        mLogger.add("stop success");
                    }

                    @Override
                    public void onFailure(int reason) {
                        mLogger.add("stop: fail: " + String.format("%d", reason));
                    }
                });
                */
            }
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            mLogger.add("WIFI_CONNECTION_CHANGED_ACTION");
            // Respond to new connection or disconnections
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            mLogger.add("WIFI_THIS_DEVICE_CHANGED_ACTION");
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
}
