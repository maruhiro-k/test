package com.example.hiroki.p2p_test.lobby.p2p;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ServerSocket {
    private java.net.ServerSocket s0;
    private String mMsg;

    public void accept(int port, final AcceptListener listener) {
        new AsyncTask<Integer, Void, Socket>() {
            @Override
            protected Socket doInBackground(Integer... params) {
                try {
                    mMsg = "new";
                    s0 = new java.net.ServerSocket();
                    mMsg = "setReuseAddress";
                    s0.setReuseAddress(true);
                    mMsg = "bind:" + s0.getReuseAddress() + ":" + params[0] + ":" + (new InetSocketAddress(params[0]));
                    s0.bind(new InetSocketAddress(params[0]));
                    mMsg = "accept";
                    return s0.accept();
                }
                catch (IOException e) {
                    mMsg += e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Socket s) {
                if (listener != null) {
                    if (s == null) {
                        listener.test(mMsg);
                    }
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
        void test(String e);
    }
}
