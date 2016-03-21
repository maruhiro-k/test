package com.example.hiroki.p2p_test.battle.protocol;

import android.util.Log;

import com.example.hiroki.p2p_test.battle.character.Player;
import com.example.hiroki.p2p_test.p2p.AsyncSocket;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * Created by hiroki on 2016/03/21.
 */
public class AuraBattleProtocol implements AsyncSocket.SocketListener {
    static final private String MAGIC = "ABPP";
    static final private byte ACTION_PACKET = 0x01;
    static final private byte RESULT_PACKET = 0x02;

    AsyncSocket mSock;
    Listener mListener;

    public AuraBattleProtocol(AsyncSocket sock) {
        mSock = sock;
        mSock.init(this);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void onSend(boolean result) {
        // 失敗したら何とかする
    }

    @Override
    public void onRecv(byte[] data) {
        String res = onRecvImpl(data);
        if (res != null) {
            Log.d("AuraBattleProtocol", "error: " + res);
        }
    }

    @Override
    public void onClose() {
    }

    private String onRecvImpl(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data);

        if (MAGIC.compareTo(new String(data, 0, 4)) != 0) {
            return "invalid head";
        }

        int len = buf.getInt(4);
        if (len != buf.remaining() - 8) {
            return "invalid length";
        }

        byte body[] = new byte[len];
        buf.get(body, 0, len);

        // ちょっとばかしの改ざんチェック
        if (getCRC(body) != buf.getLong()) {
            return "invalid CRC";
        }

        // パース
        switch (body[0]) {
            case ACTION_PACKET:
                int act = buf.getInt();
                if (mListener != null) {
                    mListener.onRecvAction(act);
                }
                break;
            case RESULT_PACKET:
                int turn_number = buf.getInt();
                Player.Status enemy = new Player.Status();
                enemy.life = buf.getInt();
                enemy.aura = buf.getInt();
                Player.Status myself = new Player.Status();
                myself.life = buf.getInt();
                myself.aura = buf.getInt();
                if (mListener != null) {
                    mListener.onRecvResult(turn_number, myself, enemy);
                }
                break;
        }

        // 成功
        return null;
    }

    public void sendDecideAction(int act) {
        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.put(ACTION_PACKET);
        buf.putInt(act);
        send(buf.array());
    }

    public void sendResult(int turn_number, Player.Status myself, Player.Status enemy) {
        ByteBuffer buf = ByteBuffer.allocate(32);
        buf.put(RESULT_PACKET);
        buf.putInt(turn_number);
        buf.putInt(myself.life);
        buf.putInt(myself.aura);
        buf.putInt(enemy.life);
        buf.putInt(enemy.aura);
        send(buf.array());
    }

    private void send(byte[] data) {
        int len = data.length;
        ByteBuffer buf = ByteBuffer.allocate(MAGIC.length() + 4 + len + 8);
        buf.put(MAGIC.getBytes());
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

    public interface Listener {
        public abstract void onRecvAction(int action);
        public abstract void onRecvResult(int turn_number, Player.Status my_data, Player.Status enemy_data);
    }
}
