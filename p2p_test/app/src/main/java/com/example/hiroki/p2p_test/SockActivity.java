package com.example.hiroki.p2p_test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.hiroki.p2p_test.p2p.AsyncSocket;
import com.example.hiroki.p2p_test.p2p.ClientSocket;
import com.example.hiroki.p2p_test.p2p.ServerSocket;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

public class SockActivity extends AppCompatActivity {
    static final int PORT = 18888;
    ServerSocket ss;
    ClientSocket cs;
    AsyncSocket.SocketListener mAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sock);

        TextView t = (TextView) findViewById(R.id.hintText);
        t.setMovementMethod(ScrollingMovementMethod.getInstance());

        String mine = "";
        try {
            java.util.Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                java.util.Enumeration<InetAddress> addresses = network.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    String address = addresses.nextElement().getHostAddress();

                    //127.0.0.1と0.0.0.0以外のアドレスが見つかったらそれを返す
                    if (!"127.0.0.1".equals(address) && !"0.0.0.0".equals(address)) {
                        mine = address;
                    }
                }
            }
        }
        catch (SocketException e) {
        }
        t.append(mine + "\n");

        final String host;
        if (mine.equals("192.168.1.11")) {
            host = "192.168.1.12";
        }
        else {
            host = "192.168.1.11";
        }
        t.append(host + "\n");

        mAction = new AsyncSocket.SocketListener() {
            @Override
            public void onSend(boolean result) {
                Log.d("SockActivity", "onSend: " + result);
            }

            @Override
            public void onRecv(byte[] data) {
                Log.d("SockActivity", "onRecv: " + data.length);
            }

            @Override
            public void onClose() {
                Log.d("SockActivity", "onClose");
            }
        };

        Button srv_btn = (Button) findViewById(R.id.srv_btn);
        srv_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SockActivity", "on server");

                ss = new ServerSocket();
                ss.accept(PORT, new ServerSocket.AcceptListener() {
                    @Override
                    public void onAccept(AsyncSocket s) {
                        if (s != null) {
                            s.start(mAction);
                        }
                    }
                });
            }
        });
        Button clt_btn = (Button) findViewById(R.id.clt_btn);
        clt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SockActivity", "on client");

                cs = new ClientSocket();
                cs.connect(host, PORT, new ClientSocket.ConnectListener() {
                    @Override
                    public void onConnect(AsyncSocket s, boolean result) {
                        if (s != null) {
                            s.start(mAction);
                        }
                    }
                });
            }
        });
    }
}
