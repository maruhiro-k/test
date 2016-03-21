package com.example.hiroki.p2p_test.battle;

import com.example.hiroki.p2p_test.battle.character.Aura;
import com.example.hiroki.p2p_test.battle.character.Player;
import com.example.hiroki.p2p_test.battle.controller.ControllerBase;
import com.example.hiroki.p2p_test.battle.protocol.BattleAction;

/**
 * Created by hiroki on 2016/03/21.
 */
public class BattleEngine {
    int mTurn = 0;
    Player mMyPlayer;
    Player mEnemyPlayer;
    Aura mMyAura;
    Aura mEnemyAura;
    int mMyAction = BattleAction.NO_ACTION;
    int mEnemyAction = BattleAction.NO_ACTION;

    private enum BattleScene {
        kOpen,
        kHello,
        kIdling,
        kAction,
        kFinish,
        kClose,
    }
    BattleScene mScene = BattleScene.kOpen;

    public BattleEngine(Player my_player, Player enemy_player) {
        mMyPlayer = my_player;
        mEnemyPlayer = enemy_player;
    }

    public void update() {
        switch (mScene) {
            case kHello:
                // todo 挨拶が終わるまで待つ
                // 初回はターン0の終了とする
                end();
                break;
            case kIdling:
                // todo タイムアウトのカウントダウン
                break;
            case kAction:
                if (false) {    // todo 相殺フレーム
                    if (isActive(mMyAura) && isActive(mEnemyAura)) {
                        // 相殺
                        mMyAura.deactive();
                        mEnemyAura.deactive();
                    }
                }
                else if (false) {   // todo Hitフレーム
                    if (isActive(mMyAura) && !isActive(mEnemyAura)) {
                        if (mEnemyAction != BattleAction.DEFENCE || mMyAura.isSuper()) {
                            // ヒット
                            mEnemyPlayer.damage(+1);
                        }
                        else {
                            // ガード
                        }
                        mMyAura.deactive();
                    }
                    // todo: mEnemyAuraにも
                }
                else if (false) {   // todo アクション終了フレーム
                    if (mMyAura != null) {
                        mMyPlayer.changeAura(-mMyAura.getPower());
                    }
                    else if (mMyAction == BattleAction.CHARGE) {
                        mMyPlayer.changeAura(+1);
                    }
                    // todo: mEnemyPlayerにも

                    end();
                }
                break;
        }

        mMyPlayer.update();
        mEnemyPlayer.update();
        mMyAura.update();
        mEnemyAura.update();
    }

    public void action(Player player, int act) {
        if (player == mMyPlayer) {
            mEnemyPlayer.notifyEnemyAction(act);
        }
        else if (player == mEnemyPlayer) {
            mMyPlayer.notifyEnemyAction(act);
        }

        if (mMyAction != BattleAction.NO_ACTION && mEnemyAction != BattleAction.NO_ACTION) {
            start_action();
        }
    }

    public void start_hello()
    {
        mMyPlayer.hello();
        mEnemyPlayer.hello();
        mScene = BattleScene.kHello;
    }

    public void start_action()
    {
        mMyAura = mMyPlayer.action();
        mEnemyAura = mEnemyPlayer.action();
        mScene = BattleScene.kAction;
    }

    private void end() {
        // 結果共有
        // ここでアイドリングか勝ちか負けか、ポーズを変える
        mMyPlayer.notifyResult(mTurn, mEnemyPlayer.getStatus());
        mEnemyPlayer.notifyResult(mTurn, mMyPlayer.getStatus());

        // 次へ
        mTurn++;
        mMyPlayer.idling();
        mEnemyPlayer.idling();
        mMyAction = BattleAction.NO_ACTION;
        mEnemyAction = BattleAction.NO_ACTION;
        mScene = BattleScene.kIdling;
    }

    private boolean isActive(Aura aura) {
        return (aura != null && aura.isActive());
    }
}
