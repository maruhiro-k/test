package com.example.hiroki.p2p_test.battle;

import com.example.hiroki.p2p_test.battle.character.Aura;
import com.example.hiroki.p2p_test.battle.character.Player;
import com.example.hiroki.p2p_test.battle.controller.ControllerBase;
import com.example.hiroki.p2p_test.battle.protocol.BattleAction;
import com.example.hiroki.p2p_test.util.Logger;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by hiroki on 2016/03/21.
 */
public class BattleEngine implements Player.Listener {
    int mTurn = 0;
    Player mMyPlayer;
    Player mEnemyPlayer;
    Aura mMyAura;
    Aura mEnemyAura;

    private enum BattleScene {
        kOpen,
        kHello,
        kIdling,
        kAction,
        kFinish,
        kClose,
    }
    BattleScene mScene = BattleScene.kOpen;
    Timer mTimer;
    long mStartMs = 0;


    public BattleEngine(Player my_player, Player enemy_player) {
        mMyPlayer = my_player;
        mEnemyPlayer = enemy_player;
        mMyPlayer.setListener(this);
        mEnemyPlayer.setListener(this);
    }

    public void start() {
        stop(); // いったん止める

        start_hello();  // 挨拶から

        // タイマー開始
        mTimer = new Timer();
        final Handler h = new Handler();

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                h.post(new Runnable(){
                    @Override
                    public void run() {
                        update();
                    }
                });
            }
        }, 0, 100);
    }

    private void stop() {
        if (mTimer!=null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void update() {
//        Log.d("update", "scene = " + mScene.toString() + ": " + getElapsedSec() + "sec");

        switch (mScene) {
            case kHello:
                if (getElapsedSec() >= 1) {
                    end_turn();  // 初回はターン0の終了とする
                }
                break;
            case kIdling:
                if (getElapsedSec() >= 30) {
                    start_action(); // タイムアウト
                }
                break;
            case kAction:
                double sec = getElapsedSec();

                if (sec >= 3) {
                    if (isActive(mMyAura) && isActive(mEnemyAura)) {
                        // 相殺
                        mMyAura.deactive();
                        mEnemyAura.deactive();
                    }
                }

                if (sec >= 6) {
                    if (isActive(mMyAura) && !isActive(mEnemyAura)) {
                        // 相手にヒット
                        if (mEnemyPlayer.getAction() != BattleAction.DEFENCE || mMyAura.isSuper()) {
                            mEnemyPlayer.damage(+1);
                        }
                        mMyAura.deactive();
                    } else if (!isActive(mMyAura) && isActive(mEnemyAura)) {
                        // 自分にヒット
                        if (mMyPlayer.getAction() != BattleAction.DEFENCE || mEnemyAura.isSuper()) {
                            mMyPlayer.damage(+1);
                        }
                        mEnemyAura.deactive();
                    }
                }

                if (sec >= 9) {
                    // オーラ増減
                    if (mMyAura != null) {
                        mMyPlayer.changeAura(-mMyAura.getPower());
                    } else if (mMyPlayer.getAction() == BattleAction.CHARGE) {
                        mMyPlayer.changeAura(+1);
                    }
                    if (mEnemyAura != null) {
                        mEnemyPlayer.changeAura(-mEnemyAura.getPower());
                    } else if (mEnemyPlayer.getAction() == BattleAction.CHARGE) {
                        mEnemyPlayer.changeAura(+1);
                    }

                    // アクション終了
                    end_turn();
                }
                break;
            case kFinish:
                break;
        }

        mMyPlayer.update();
        mEnemyPlayer.update();
        if (mMyAura!=null) {
            mMyAura.update();
        }
        if (mEnemyAura!=null) {
            mEnemyAura.update();
        }
    }

    public void action(Player player, int act) {
        if (player == mMyPlayer) {
            echo("my action="+act);
            mEnemyPlayer.notifyEnemyAction(act);
        }
        else if (player == mEnemyPlayer) {
            echo("enemy action="+act);
            mMyPlayer.notifyEnemyAction(act);
        }

        if (mMyPlayer.getAction() != BattleAction.NO_ACTION && mEnemyPlayer.getAction() != BattleAction.NO_ACTION) {
            echo("start_action");
            start_action();
        }
    }

    public void start_hello() {
        mMyPlayer.hello();
        mEnemyPlayer.hello();
        mScene = BattleScene.kHello;
        mTurn = 0;
        mStartMs = System.currentTimeMillis();
    }

    public void start_action() {
        mMyAura = mMyPlayer.action();
        mEnemyAura = mEnemyPlayer.action();
        mScene = BattleScene.kAction;
        mStartMs = System.currentTimeMillis();
        echo("M: act=" + mMyPlayer.getAction() + ", aura=" + mMyAura);
        echo("E: act=" + mEnemyPlayer.getAction() + ", aura=" + mEnemyAura);
    }

    private void end_turn() {
        // 結果共有
        Player.Status stE = mEnemyPlayer.getStatus();
        Player.Status stM = mMyPlayer.getStatus();
        mMyPlayer.notifyResult(mTurn, stE);
        mEnemyPlayer.notifyResult(mTurn, stM);


        if (stM.life > 0 && stE.life > 0) {
            // 次のターンへ
            mTurn++;
            mMyPlayer.idling();
            mEnemyPlayer.idling();
            mScene = BattleScene.kIdling;
            echo("next turn");
        }
        else {
            // 決着
            mMyPlayer.finish();
            mEnemyPlayer.finish();
            mScene = BattleScene.kFinish;
            echo("finish turn");
        }

        mStartMs = System.currentTimeMillis();
    }

    private double getElapsedSec() {
        return (System.currentTimeMillis() - mStartMs) / 1000.0;
    }

    private boolean isActive(Aura aura) {
        return (aura != null && aura.isActive());
    }
/*
        // 攻撃
        Button atk_btn = (Button) findViewById(R.id.attack_button);
        atk_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // オーラがあれば攻撃
                // オーラが満タンなら必殺
                // battle_engine.attack(aura);
            }
        });

        // 防御
        Button def_btn = (Button) findViewById(R.id.def_button);
        def_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 攻撃ならセーフ
                // 必殺ならアウト
                // battle_engine.defence();
            }
        });

        // ためる
        Button chg_btn = (Button) findViewById(R.id.charge_button);
        chg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // オーラ＋１
                // 無防備
                // battle_engine.charge();
            }
        });
*/

    private Logger mLogger;
    public void setLogView(TextView v) {
        mLogger = new Logger("BattleEngine", v);
        echo("log start!");
    }
    private void echo(String log) {
        if (mLogger != null) {
            mLogger.add(log);
        }
    }
}
