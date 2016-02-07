package com.example.hiroki.p2p_test.p2p;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by hiroki on 2016/02/08.
 */
public class AsyncSocket {
    Socket s;
    SocketListener mListener;

    AsyncSocket(Socket s) {
        this.s = s;
    }

    public void start(SocketListener listener) {
        if (!isConnected()) {
            if (mListener != null) {
                mListener.onClose();
            }
            return;
        }

        this.mListener = listener;

        new AsyncTask<Void, byte[], Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    InputStream in = s.getInputStream();
                    byte[] buf = new byte[1024];
                    try {
                        while (true) {
                            int sz = in.read(buf);
                            if (sz > 0) {
                                publishProgress(buf);
                            }
                        }
                    }
                    catch (IOException e) {
                        Log.d("socket", e.getMessage());
                    }
                    if (in != null) {
                        in.close();
                    }
                }
                catch (IOException e) {
                    Log.d("socket", e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Log.e("socket", "end");
                if (mListener != null) {
                    mListener.onClose();
                }
            }

            @Override
            protected void onProgressUpdate(byte[]... values) {
                if (mListener != null) {
                    mListener.onRecv(values[0]);
                }
            }
        }.execute();
    }

    public void send(byte[] data) {
        if (!isConnected()) {
            if (mListener != null) {
                Log.e("socket", "not connected");
                mListener.onSend(false);
            }
            return;
        }

        new AsyncTask<byte[], Void, Boolean>() {
            @Override
            protected Boolean doInBackground(byte[]... params) {
                try {
                    OutputStream out = s.getOutputStream();
                    out.write(params[0]);
                    out.close();
                    return Boolean.TRUE;
                }
                catch (IOException e) {
                    return Boolean.FALSE;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                Log.e("socket", "send: " + result);
                if (mListener != null) {
                    mListener.onSend(result);
                }
            }
        }.execute(data);
    }

    boolean isConnected() {
        if (s == null) {
            return false;
        }
        if (!s.isConnected()) {
            return false;
        }
        return true;
    }

    public interface SocketListener {
        void onSend(boolean result);
        void onRecv(byte[] data);
        void onClose();
    }
}
