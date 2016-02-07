package com.example.hiroki.p2p_test.p2p;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by hiroki on 2016/02/08.
 */
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
                }
                catch (IOException e) {
                    Log.d("ClientSocket", e.getMessage());
                    return Boolean.FALSE;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                Log.d("ClientSocket", "connect: " + result);
                if (listener != null) {
                    listener.onConnect(ClientSocket.this, result);
                }
            }
        }.execute(new InetSocketAddress(host, port));
    }

    public interface ConnectListener {
        void onConnect(AsyncSocket s, boolean result);
    }
}
