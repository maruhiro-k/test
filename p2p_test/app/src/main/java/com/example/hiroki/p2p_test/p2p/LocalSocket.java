package com.example.hiroki.p2p_test.p2p;

import android.util.Log;

import com.example.hiroki.p2p_test.battle.character.Player;
import com.example.hiroki.p2p_test.battle.protocol.AuraBattleProtocol;
import com.example.hiroki.p2p_test.battle.protocol.BattleAction;
import com.example.hiroki.p2p_test.p2p.AsyncSocket;

import java.util.Random;

/**
 * Created by hiroki on 2016/11/05.
 * This is dummy socket for development.
 */

public class LocalSocket extends AsyncSocket {
    ClientSocket cs;
    AuraBattleProtocol mABP;
    Random r;

    LocalSocket() {
        super(null);
        r = new Random(System.currentTimeMillis());
    }

    public void setAcceptSocket(AsyncSocket as) {
        super.s = as.s;
    }

    public void connect(int port) {
        cs = new ClientSocket();
        cs.connect("127.0.0.1", port, new ClientSocket.ConnectListener(){
            @Override
            public void onConnect(boolean result) {

                // 内部にコマンドを送るプレイヤーを内包
                mABP = new AuraBattleProtocol(cs);
                mABP.setListener(new AuraBattleProtocol.Listener() {
                    @Override
                    public void onRecvAction(int act) {
                        // action
                        mABP.sendDecideAction(r.nextInt(4) + 1);
                    }

                    @Override
                    public void onRecvResult(int turn_number, Player.Status my_data, Player.Status enemy_data) {
                        // 次
                        // 結果が正しいか調べる？
                        // 表示に反映
                        mABP.sendResult(turn_number, my_data, enemy_data);
                    }
                });
            }
        });
    }
}
