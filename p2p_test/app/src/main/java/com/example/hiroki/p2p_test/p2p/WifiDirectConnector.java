package com.example.hiroki.p2p_test.p2p;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by hiroki on 2016/01/10.
 */
public class WifiDirectConnector implements Connector {
    Context c;

    public WifiDirectConnector(Context c) {
        this.c = c;
        Log.d("WifiDirectConnector", "create");
    }

    @Override
    public boolean isEnabled() {
        return c.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT);
    }
}
