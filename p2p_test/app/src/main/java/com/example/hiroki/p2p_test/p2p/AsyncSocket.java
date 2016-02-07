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
            Log.d("socket", "not started");
            return;
        }

        this.mListener = listener;

        Log.d("socket", "async read");
        new AsyncTask<Void, byte[], Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Log.d("recv", "doInBackground");
                try {
                    InputStream in = s.getInputStream();
                    byte[] buf = new byte[1024];
                    Log.d("recv", "InputStream: " + in);
                    try {
                        while (true) {
                            Log.d("recv", "read");
                            int sz = in.read(buf);
                            if (sz > 0) {
                                Log.d("recv", "publishProgress");
                                publishProgress(buf);
                            }
                        }
                    }
                    catch (IOException e) {
                        Log.d("socket2", e.getMessage());
                    }
                    Log.d("recv", "end");
                    if (in != null) {
                        in.close();
                    }
                }
                catch (IOException e) {
                    Log.d("socket1", e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Log.d("socket", "end");
                if (mListener != null) {
                    mListener.onClose();
                }
            }

            @Override
            protected void onProgressUpdate(byte[]... values) {
                Log.d("recv", "onProgressUpdate: " + values[0].length);
                if (mListener != null) {
                    mListener.onRecv(values[0]);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void send(byte[] data) {
        if (!isConnected()) {
            Log.d("socket", "not connected");
            if (mListener != null) {
                mListener.onSend(false);
            }
            return;
        }

        Log.d("socket", "send async");
        new AsyncTask<byte[], Void, Boolean>() {
            @Override
            protected Boolean doInBackground(byte[]... params) {
                Log.d("send", "doInBackground");
                try {
                    OutputStream out = s.getOutputStream();
                    Log.d("send", "write: " + params[0][0] + ", " + params[0].length);
                    out.write(params[0]);
                    out.close();
                    return Boolean.TRUE;
                }
                catch (IOException e) {
                    Log.d("send", e.getMessage());
                    return Boolean.FALSE;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                Log.d("socket", "send: " + result);
                if (mListener != null) {
                    mListener.onSend(result);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data.clone());
    }

    public void close() {
        try {
            if (s != null) {
                s.close();
            }
        } catch (IOException e) {
            Log.d("close", e.getMessage());
        }
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
