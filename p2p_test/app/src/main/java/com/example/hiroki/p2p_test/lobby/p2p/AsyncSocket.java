package com.example.hiroki.p2p_test.lobby.p2p;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class AsyncSocket {
    protected Socket s;
    private SocketListener mListener;

    AsyncSocket(Socket s) {
        this.s = s;
    }

    boolean init(SocketListener listener) {
        if (!isConnected()) {
            return false;
        }

        this.mListener = listener;
        /*
        try {
            //mLogger.add("getReuseAddress " + s.getReuseAddress());
            // サーバソケットでバインドできない例外が出ててたらこれ設定してみると良いかも
        }
        catch(SocketException e) {
            //mLogger.add(e.getMessage());
        }
        */

        // 受信開始
        new AsyncTask<Void, byte[], Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    InputStream in = s.getInputStream();
                    byte[] buf = new byte[1024];
                    int sz;
                    while ((sz = in.read(buf)) > 0) {
                        //mLogger.add("read: size=" + sz);
                        publishProgress(Arrays.copyOfRange(buf, 0, sz));
                    }
                }
                catch (IOException e) {
                    //mLogger.add("read error: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                //mLogger.add("recv onPostExecute");
                onClose();
            }

            @Override
            protected void onProgressUpdate(byte[]... values) {
                onRecv(values[0]);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return true;
    }

    void send(byte[] data) {
        if (!isConnected()) {
            onSend(false);
            return;
        }

        // 送信
        new AsyncTask<byte[], Void, Boolean>() {
            @Override
            protected Boolean doInBackground(byte[]... params) {
                try {
                    OutputStream out = s.getOutputStream();
                    //mLogger.add("call write: " + params[0][0] + ", " + params[0].length);
                    out.write(params[0]);
                    return Boolean.TRUE;
                }
                catch (IOException e) {
                    //mLogger.add("send error: " + e.getMessage());
                    return Boolean.FALSE;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                onSend(result);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data.clone());
    }

    public void close() {
        try {
            if (s != null) {
                s.close();
                onClose();
            }
        } catch (IOException e) {
            //mLogger.add("close error: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        if (s == null) {
            return false;
        }
        else if (!s.isConnected()) {
            return false;
        }
        else if (s.isClosed()) {
            return false;
        }
        return true;
    }

    private void onSend(boolean result) {
        if (mListener != null) {
            mListener.onSend(result);
        }
    }

    private void onRecv(byte[] data) {
        if (mListener != null) {
            mListener.onRecv(data);
        }
    }

    private void onClose() {
        s = null;
        if (mListener != null) {
            mListener.onClose();
        }
    }

    interface SocketListener {
        void onSend(boolean result);
        void onRecv(byte[] data);
        void onClose();
    }
}
