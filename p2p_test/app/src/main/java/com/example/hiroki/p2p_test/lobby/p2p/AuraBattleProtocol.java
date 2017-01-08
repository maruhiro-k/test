package com.example.hiroki.p2p_test.lobby.p2p;

import com.example.hiroki.p2p_test.battle.character.Battler;

import java.nio.ByteBuffer;

/**
 * Created by hiroki on 2016/03/21.
 */
public class AuraBattleProtocol implements AsyncSocket.SocketListener {
    static final public String APP_MAGIC = "ABP1";
    static final private byte ACTION_PACKET = 0x02;     //!< 対戦中の行動（BattleAction参照）
    static final private byte RESULT_PACKET = 0x03;     //!< ターンの区切りに送る（ターン数、それぞれのライフと残気）

    private WifiSocketProtocol mWSP;
    private Listener mListener;

    public AuraBattleProtocol(AsyncSocket sock) {
        mWSP = new WifiSocketProtocol(sock, APP_MAGIC);
        mWSP.setListener(this);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void sendDecideAction(int act) {
        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.put(ACTION_PACKET);
        buf.putInt(act);
        mWSP.send(buf.array());
    }

    public void sendResult(int turn_number, Battler.Status myself, Battler.Status enemy) {
        ByteBuffer buf = ByteBuffer.allocate(32);
        buf.put(RESULT_PACKET);
        buf.putInt(turn_number);
        buf.putInt(myself.life);
        buf.putInt(myself.aura);
        buf.putInt(enemy.life);
        buf.putInt(enemy.aura);
        mWSP.send(buf.array());
    }

    @Override
    public void onSend(boolean result) {

    }

    @Override
    public void onRecv(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data);

        switch (buf.get()) {
            case ACTION_PACKET: // 行動
                int act = buf.getInt();
                if (mListener != null) {
                    mListener.onRecvAction(act);
                }
                break;

            case RESULT_PACKET: // ターン終了同期
                int turn_number = buf.getInt();
                Battler.Status myself = new Battler.Status();
                myself.life = buf.getInt();
                myself.aura = buf.getInt();
                Battler.Status enemy = new Battler.Status();
                enemy.life = buf.getInt();
                enemy.aura = buf.getInt();
                if (mListener != null) {
                    mListener.onRecvResult(turn_number, myself, enemy);
                }
                break;
        }
    }

    @Override
    public void onClose() {

    }

    public interface Listener {
        void onRecvAction(int action);
        void onRecvResult(int turn_number, Battler.Status my_data, Battler.Status enemy_data);
    }
}
