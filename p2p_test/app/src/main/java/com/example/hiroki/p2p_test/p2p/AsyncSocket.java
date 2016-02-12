package com.example.hiroki.p2p_test.p2p;

import android.os.AsyncTask;

import com.example.hiroki.p2p_test.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class AsyncSocket {
    Socket s;
    SocketListener mListener;
    Logger mLogger;

    AsyncSocket(Socket s) {
        this.s = s;
    }

    public void init(SocketListener listener) {
        this.mListener = listener;
    }

    public void recv() {
        if (!isConnected()) {
            if (mListener != null) {
                mListener.onClose();
            }
            mLogger.add("not started");
            return;
        }

        new AsyncTask<Void, byte[], Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    InputStream in = s.getInputStream();
                    byte[] buf = new byte[1024];
                    try {
                        while (true) {
                            mLogger.add("call read");
                            int sz = in.read(buf);
                            mLogger.add("read: size=" + sz);
                            if (sz > 0) {
                                publishProgress(buf);
                            }
                            if (sz == -1) {
                                break;
                            }
                        }
                    }
                    catch (IOException e) {
                        mLogger.add("read error: " + e.getMessage());
                    }
                    mLogger.add("recv end");
                    if (in != null) {
                        mLogger.add("recv closing");
                        in.close();
                    }
                }
                catch (IOException e) {
                    mLogger.add("read2 error: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mLogger.add("recv onPostExecute");
                if (mListener != null) {
                    //mListener.onClose();
                }
            }

            @Override
            protected void onProgressUpdate(byte[]... values) {
                mLogger.add("recv onProgressUpdate: " + values[0].length);
                if (mListener != null) {
                    mListener.onRecv(values[0]);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void send(byte[] data) {
        if (!isConnected()) {
            mLogger.add("send error: not connected");
            if (mListener != null) {
                mListener.onSend(false);
            }
            return;
        }

        new AsyncTask<byte[], Void, Boolean>() {
            @Override
            protected Boolean doInBackground(byte[]... params) {
                try {
                    OutputStream out = s.getOutputStream();
                    mLogger.add("call write: " + params[0][0] + ", " + params[0].length);
                    out.write(params[0]);
                    out.close();
                    return Boolean.TRUE;
                }
                catch (IOException e) {
                    mLogger.add("send error: " + e.getMessage());
                    return Boolean.FALSE;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                mLogger.add("send onPostExecute: " + result);
                if (mListener != null) {
                    mListener.onSend(result);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data.clone());
    }

    public void close() {
        try {
            if (s != null) {
                mLogger.add("call close");
                s.close();
                mLogger.add("called close");
                if (mListener != null) {
                    mLogger.add("call onClose");
                    mListener.onClose();
                }
            }
        } catch (IOException e) {
            mLogger.add("close error: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        if (s == null) {
            return false;
        }
        if (!s.isConnected()) {
            return false;
        }
        if (s.isClosed()) {
            return false;
        }
        return true;
    }

    public void addLogger(Logger logger) {
        mLogger = logger;
    }

    public interface SocketListener {
        void onSend(boolean result);
        void onRecv(byte[] data);
        void onClose();
    }
}
