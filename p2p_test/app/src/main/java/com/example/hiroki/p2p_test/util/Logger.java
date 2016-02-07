package com.example.hiroki.p2p_test.util;

import android.util.Log;
import android.widget.TextView;

/**
 * Created by hiroki on 2016/02/08.
 */
public class Logger {
    String tag;
    TextView t = null;
    int num = 0;

    public Logger(String tag, TextView t) {
        this.tag = tag;
        this.t = t;
    }

    public void add(String log) {
        Log.d(tag, log);
        if (t != null) {
            t.append(++num + ": " + log + "\n");
        }
    }
}
