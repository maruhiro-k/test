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

import com.example.hiroki.p2p_test.util.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    IntentFilter mIntentFilter;
    Context mContext;
    Collection<WifiP2pDevice> mDecices;
    Logger mLogger;
    ServerSocket ss;
    ClientSocket cs;
    AsyncSocket as;

    public interface LogAction {
        public abstract void add(String log);
    }

    public WiFiDirectBroadcastReceiver(Context c, Logger logger) {
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
        config.wps.setup = WpsInfo.PBC; // これをつけたらつながりやすくなった（なくてもつながってたことがあったけど謎）

        mLogger.add("begin connect:\n" + dev.toString());
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
                mLogger.add("connect success");
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

        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mLogger.add("cancelConnect success");
            }

            @Override
            public void onFailure(int reason) {
                mLogger.add("cancelConnect failed: " + errStr(reason));
            }
        });

        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mLogger.add("removeGroup success");
            }

            @Override
            public void onFailure(int reason) {
                mLogger.add("removeGroup failed: " + errStr(reason));
            }
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

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
                            mLogger.add("found peers: " + dev.deviceName + " " + statusStr(dev.status));
                        }
                    }
                });
            }
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            mLogger.add("WIFI_CONNECTION_CHANGED_ACTION");
            // Respond to new connection or disconnections

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && networkInfo.isConnected()) {
                mLogger.add("connected");

                WifiP2pInfo p2pInfo = (WifiP2pInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                if (p2pInfo != null) {
                    if (ss != null) {
                        mLogger.add("already server!");
                        return;
                    }
                    if (cs != null) {
                        mLogger.add("already client!");
                        return;
                    }

                    String addr = p2pInfo.groupOwnerAddress.getHostAddress();
                    if (p2pInfo.isGroupOwner) {
                        // server
                        mLogger.add("owner is myself = " + addr);

                        ss = new ServerSocket();
                        ss.addLogger(mLogger);
                        ss.accept(12345, new ServerSocket.AcceptListener() {
                            @Override
                            public void onAccept(AsyncSocket s) {
                                mLogger.add("accept: " + s);
                                as = s;
                                as.init(new AsyncSocket.SocketListener() {
                                    @Override
                                    public void onSend(boolean result) {

                                    }

                                    @Override
                                    public void onRecv(byte[] data) {
                                        mLogger.add("recv: " + Arrays.toString(data));
                                    }

                                    @Override
                                    public void onClose() {

                                    }
                                });
                                byte[] data = {0x10, 0x11, 0x12};
                                as.send(data);
                            }
                        });
                    }
                    else {
                        // client
                        mLogger.add("owner address = " + addr);

                        cs = new ClientSocket();
                        cs.addLogger(mLogger);
                        cs.connect(addr, 12345, new ClientSocket.ConnectListener() {
                            @Override
                            public void onConnect(boolean result) {
                                mLogger.add("connect: " + result);
                                cs.init(new AsyncSocket.SocketListener() {
                                    @Override
                                    public void onSend(boolean result) {

                                    }

                                    @Override
                                    public void onRecv(byte[] data) {
                                        mLogger.add("recv: " + Arrays.toString(data));
                                    }

                                    @Override
                                    public void onClose() {

                                    }
                                });
                                byte[] data = {0x0, 0x1, 0x2};
                                cs.send(data);
                            }
                        });
                    }
                }
                else {
                    mLogger.add("host address = null");
                }
            }
            else {
                mLogger.add("disconnected");
                // close
            }
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
}
