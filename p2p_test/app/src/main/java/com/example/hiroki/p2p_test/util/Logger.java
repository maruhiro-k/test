package com.example.hiroki.p2p_test.util;

import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

public class Logger {
    String tag;
    Handler h;
    TextView t = null;
    int num = 0;

    public Logger(String tag, TextView t) {
        this.tag = tag;
        h = new Handler();
        if (t != null) {
            t.setMovementMethod(ScrollingMovementMethod.getInstance());
            this.t = t;
        }
    }

    public void add(final String log) {

        if (Thread.currentThread().equals(h.getLooper().getThread())) {
            Log.d(tag, log + " [L]");
            add_(log);
        }
        else {
            Log.d(tag, log + " [A]");
            h.post(new Runnable() {
                @Override
                public void run() {
                    add_(log);
                }
            });
        }
    }

    void add_(final String log) {
        if (t != null) {
            t.append(++num + ": " + log + "\n");
            int dL = t.getLineCount() - t.getMaxLines();
            if (dL > 0) {
                t.setScrollY(dL * t.getLineHeight());
            }
        }
    }
}
