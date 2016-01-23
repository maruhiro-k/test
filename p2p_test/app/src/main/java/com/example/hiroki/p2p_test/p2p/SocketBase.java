package com.example.hiroki.p2p_test.p2p;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

/**
 * Created by hiroki on 2016/01/23.
 */
public abstract class SocketBase extends AsyncTask {
    private CloseListener mCloser;

    public abstract void close();

    public void close(CloseListener closer) {
        mCloser = closer;
        close();
    }

    @Override
    protected void onCancelled() {
        Log.d("SocketBase", "onCancelled");
        onClosed();
    }

    @Override
    protected void onPostExecute(Object o) {
        Log.d("SocketBase", "onPostExecute");
        onClosed();
    }

    protected void onClosed() {
        Log.d("SocketBase", "onClosed");
        if (mCloser != null) {
            mCloser.onClosed(this);
            mCloser = null;
        }
    }

    protected interface CloseListener {
        abstract public void onClosed(SocketBase s);
    }
}
