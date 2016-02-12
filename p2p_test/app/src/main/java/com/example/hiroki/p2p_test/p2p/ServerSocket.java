package com.example.hiroki.p2p_test.p2p;

import android.os.AsyncTask;

import com.example.hiroki.p2p_test.util.Logger;

import java.io.IOException;
import java.net.Socket;

public class ServerSocket {
    private java.net.ServerSocket s0;
    Logger mLogger;

    public void addLogger(Logger logger) {
        mLogger = logger;
    }

    public void accept(int port, final AcceptListener listener) {
        new AsyncTask<Integer, Void, Socket>() {
            @Override
            protected Socket doInBackground(Integer... params) {
                try {
                    s0 = new java.net.ServerSocket(params[0]);
                    mLogger.add("call accept");
                    return s0.accept();
                }
                catch (IOException e) {
                    mLogger.add("accept error: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Socket s) {
                if (listener != null) {
                    AsyncSocket as = new AsyncSocket(s);
                    as.addLogger(mLogger);
                    listener.onAccept(as);
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
            mLogger.add("close error: " + e.getMessage());
        }
    }

    public interface AcceptListener {
        void onAccept(AsyncSocket s);
    }
}
