package com.example.hiroki.p2p_test.p2p;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by hiroki on 2016/01/23.
 */
public class ServerSocket {
    private java.net.ServerSocket s0;

    public void accept(int port, final AcceptListener listener) {
        new AsyncTask<Integer, Void, Socket>() {
            @Override
            protected Socket doInBackground(Integer... params) {
                try {
                    s0 = new java.net.ServerSocket(params[0]);
                    return s0.accept();
                }
                catch (IOException e) {
                    Log.d("server", e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Socket s) {
                Log.d("server", "accept: " + s);
                if (listener != null) {
                    listener.onAccept(new AsyncSocket(s));
                }
            }
        }.execute(port);
    }

    public void close() {
        try {
            if (s0 != null) {
                s0.close();
            }
        } catch (IOException e) {
            Log.d("close", e.getMessage());
        }
    }

    public interface AcceptListener {
        void onAccept(AsyncSocket s);
    }
}
