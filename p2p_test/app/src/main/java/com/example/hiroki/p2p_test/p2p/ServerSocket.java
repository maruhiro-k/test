package com.example.hiroki.p2p_test.p2p;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by hiroki on 2016/01/23.
 */
public class ServerSocket extends SocketBase {
    static final int PORT = 8888;
    private java.net.ServerSocket s0;

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            s0 = new java.net.ServerSocket(PORT);
            Log.d("accept", "begin accept");
            Socket client = s0.accept();
            Log.d("accept", "end accept: " + client);

            InputStream in = client.getInputStream();
            byte buf[] = new byte[1024];
            while (in.read(buf) > 0) {
                String str = new String(buf, "UTF-8");
                Log.d("read server", str + "\n" + buf.toString());
            }
            in.close();

            OutputStream out = client.getOutputStream();
            out.write("answer grea".getBytes("UTF-8"));
            out.close();
        }
        catch (IOException e) {
            Log.e("server", e.getMessage());
        }
        s0 = null;
        Log.d("test", "end");
        return null;
    }

    public void close() {
        if (s0 != null) {
            try {
                s0.close();
                return;
            } catch (IOException e) {
                Log.e("close", e.getMessage());
            }
        }
        cancel(true);
    }
}
