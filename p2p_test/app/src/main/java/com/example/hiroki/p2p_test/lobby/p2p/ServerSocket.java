package com.example.hiroki.p2p_test.lobby.p2p;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.Socket;

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
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Socket s) {
                if (listener != null) {
                    AsyncSocket as = new AsyncSocket(s);
                    listener.onAccept(as);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, port);
    }

    public void close() {
        try {
            if (s0 != null) {
                s0.close();
            }
        } catch (IOException e) {
        }
    }

    public interface AcceptListener {
        void onAccept(AsyncSocket s);
    }
}
