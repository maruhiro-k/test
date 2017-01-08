package com.example.hiroki.p2p_test.lobby.p2p;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSocket extends AsyncSocket {

    public ClientSocket() {
        super(new Socket());
    }

    public void connect(String host, int port, final ConnectListener listener) {
        new AsyncTask<InetSocketAddress, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(InetSocketAddress[] params) {
                try {
                    s.bind(null);
                    s.connect(params[0], 10000);
                    return Boolean.TRUE;
                } catch (IOException e) {
                    return Boolean.FALSE;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (listener != null) {
                    listener.onConnect(result);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new InetSocketAddress(host, port));
    }

    public interface ConnectListener {
        void onConnect(boolean result);
    }
}
