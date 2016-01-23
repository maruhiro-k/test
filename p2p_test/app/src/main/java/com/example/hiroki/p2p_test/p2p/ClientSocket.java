package com.example.hiroki.p2p_test.p2p;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by hiroki on 2016/01/23.
 */
public class ClientSocket extends SocketBase {
    Socket s;

    @Override
    protected Object doInBackground(Object[] params) {
        InetAddress host = (InetAddress)params[0];
        Log.d("client0", "host="  + host);
        try {
            s = new Socket();
            s.bind(null);
            s.connect(new InetSocketAddress(host, 8888), 1000);

            OutputStream out = s.getOutputStream();
            out.write("test grea".getBytes("UTF-8"));

            InputStream in = s.getInputStream();
            byte buf[] = new byte[1024];
            while (in.read(buf) > 0) {
                String str = new String(buf, "UTF-8");
                Log.d("read client", str + "\n" + buf.toString());
            }
            in.close();
        }
        catch (IOException e) {
            Log.e("client", e.getMessage());
        }
        return null;
    }

    public void close() {
        if (s != null) {
            try {
                s.close();
                return;
            } catch (IOException e) {
                Log.e("close", e.getMessage());
            }
        }
        cancel(true);
    }
}
