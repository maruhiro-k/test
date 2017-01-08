package com.example.hiroki.p2p_test.lobby.p2p;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * Created by hiroki on 2016/11/13.
 */

public class WifiSocketProtocol implements AsyncSocket.SocketListener {
    static private final String LIB_MAGIC = "WSP1"; // このライブラリのプロトコルであることを宣言
    static public final int SOCK_PORT = 12344;     // 接続ポート
    private final String mAppMagic;                     // アプリの識別用文字列
    private final AsyncSocket mSock;
    private AsyncSocket.SocketListener mListener;

    WifiSocketProtocol(AsyncSocket s, String appMagic) {
        mSock = s;
        mAppMagic = appMagic;
        if (mSock == null) {
            mSock.init(this);
        }
        mSock.init(this);
    }

    public void setListener(AsyncSocket.SocketListener listener) {
        mListener = listener;
    }

    void send(byte[] data) {
        int len = data.length;
        ByteBuffer buf = ByteBuffer.allocate(LIB_MAGIC.length() + mAppMagic.length() + Integer.SIZE/8 + len + Long.SIZE/8);
        buf.put(LIB_MAGIC.getBytes());
        buf.put(mAppMagic.getBytes());
        buf.putInt(len);
        buf.put(data);
        buf.putLong(getCRC(data));
        mSock.send(buf.array());
    }

    private long getCRC(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    @Override
    public void onSend(boolean result) {
        if (mListener != null) {
            mListener.onSend(result);
        }
    }

    @Override
    public void onRecv(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data);

        // ヘッダ確認
        String magic = new String(data, 0, LIB_MAGIC.length() + mAppMagic.length());
        if (magic.compareTo(LIB_MAGIC + mAppMagic) != 0) {
            return;
        }
        buf.position(LIB_MAGIC.length() + mAppMagic.length());

        // サイズ確認
        int len = buf.getInt();
        if (len + Long.SIZE/8 != buf.remaining()) {
            return;
        }

        // CRCチェック
        byte body[] = new byte[len];
        buf.get(body, 0, len);
        if (getCRC(body) != buf.getLong()) {
            return;
        }

        // チェックOK
        if (mListener != null) {
            mListener.onRecv(body);
        }
    }

    @Override
    public void onClose() {
        if (mListener != null) {
            mListener.onClose();
        }
    }
}
