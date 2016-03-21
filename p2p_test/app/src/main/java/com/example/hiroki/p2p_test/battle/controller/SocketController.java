package com.example.hiroki.p2p_test.battle.controller;

import com.example.hiroki.p2p_test.battle.character.Player;
import com.example.hiroki.p2p_test.battle.protocol.AuraBattleProtocol;
import com.example.hiroki.p2p_test.p2p.AsyncSocket;

/**
 * Created by hiroki on 2016/03/21.
 */
public class SocketController extends ControllerBase {
    AuraBattleProtocol mABP;

    public SocketController(AsyncSocket sock, ControllerBase.Listener listener) {
        super(listener);

        mABP = new AuraBattleProtocol(sock);
        mABP.setListener(new AuraBattleProtocol.Listener() {
            @Override
            public void onRecvAction(int act) {
                // action
                action(act);
            }

            @Override
            public void onRecvResult(int turn_number, Player.Status my_data, Player.Status enemy_data) {
                // 次
                // 結果が正しいか調べる？
                // 表示に反映
            }
        });
    }

    @Override
    protected void begin_turn(int turn_number, Player.Status my_data, Player.Status enemy_data) {
        mABP.sendResult(turn_number, my_data, enemy_data);
    }

    @Override
    protected void end_turn(int act) {
        // 何もしない

    }

    @Override
    public void notifyDecideAction(int act) {
        mABP.sendDecideAction(act);
    }
}
