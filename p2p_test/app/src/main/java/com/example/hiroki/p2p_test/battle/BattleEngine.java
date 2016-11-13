package com.example.hiroki.p2p_test.battle;

import com.example.hiroki.p2p_test.battle.character.Aura;
import com.example.hiroki.p2p_test.battle.character.Battler;
import com.example.hiroki.p2p_test.battle.protocol.BattleAction;
import com.example.hiroki.p2p_test.util.Logger;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.TextView;

/**
 * Created by hiroki on 2016/03/21.
 */
public class BattleEngine implements Battler.Listener {
    int mTurn = 0;
    Battler mMyBattler;
    Battler mEnemyBattler;
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


    public BattleEngine(Battler my_battler, Battler enemy_battler) {
        mMyBattler = my_battler;
        mEnemyBattler = enemy_battler;
        mMyBattler.setListener(this);
        mEnemyBattler.setListener(this);
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
        switch (mScene) {
            case kHello:
                if (getElapsedSec() >= 1) {
                    end_turn();  // 初回はターン0の終了とする
                }
                break;
            case kIdling:
                // todo タイムアウトすべき
                // でも同時に入力された場合の整合性が難しいぞ
                break;
            case kAction:
                double sec = getElapsedSec();

                if (sec >= 1) {
                    if (isActive(mMyAura) && isActive(mEnemyAura)) {
                        // 相殺
                        mMyAura.deactive();
                        mEnemyAura.deactive();
                        echo("相殺！");
                    }
                }

                if (sec >= 2) {
                    if (isActive(mMyAura) && !isActive(mEnemyAura)) {
                        // 相手にヒット
                        if (mEnemyBattler.getAction() != BattleAction.DEFENCE || mMyAura.isSuper()) {
                            mEnemyBattler.damage(+1);
                            echo("hit E.Life=" + mEnemyBattler.getStatus().life);
                        }
                        else {
                            echo("guard E.Life=" + mEnemyBattler.getStatus().life);
                        }
                        mMyAura.deactive();
                    } else if (!isActive(mMyAura) && isActive(mEnemyAura)) {
                        // 自分にヒット
                        if (mMyBattler.getAction() != BattleAction.DEFENCE || mEnemyAura.isSuper()) {
                            mMyBattler.damage(+1);
                            echo("hit M.Life=" + mMyBattler.getStatus().life);
                        }
                        else {
                            echo("guard M.Life=" + mMyBattler.getStatus().life);
                        }
                        mEnemyAura.deactive();
                    }
                }

                if (sec >= 3) {
                    // オーラ増減
                    if (mMyAura != null) {
                        mMyBattler.changeAura(-mMyAura.getPower());
                        echo("down M.Aura=" + mMyBattler.getStatus().aura);
                    } else if (mMyBattler.getAction() == BattleAction.CHARGE) {
                        mMyBattler.changeAura(+1);
                        echo("up M.Aura=" + mMyBattler.getStatus().aura);
                    }
                    if (mEnemyAura != null) {
                        mEnemyBattler.changeAura(-mEnemyAura.getPower());
                        echo("down E.Aura=" + mEnemyBattler.getStatus().aura);
                    } else if (mEnemyBattler.getAction() == BattleAction.CHARGE) {
                        mEnemyBattler.changeAura(+1);
                        echo("up E.Aura=" + mEnemyBattler.getStatus().aura);
                    }

                    // アクション終了
                    end_turn();
                }
                break;
            case kFinish:
                if (getElapsedSec() >= 1) {
                    close_battle();
                }
                break;
            case kClose:
                // 何もしない
                break;
        }

        mMyBattler.update();
        mEnemyBattler.update();
        if (mMyAura!=null) {
            mMyAura.update();
        }
        if (mEnemyAura!=null) {
            mEnemyAura.update();
        }
    }

    public void action(Battler battler, int act) {
        if (battler == mMyBattler) {
            echo("my action="+act);
            mEnemyBattler.notifyEnemyAction(act);
        }
        else if (battler == mEnemyBattler) {
            echo("enemy action="+act);
            mMyBattler.notifyEnemyAction(act);
        }

        if (mMyBattler.getAction() != BattleAction.NO_ACTION && mEnemyBattler.getAction() != BattleAction.NO_ACTION) {
            start_action();
        }
    }

    public void start_hello() {
        mMyBattler.hello();
        mEnemyBattler.hello();
        mScene = BattleScene.kHello;
        mTurn = 0;
        mStartMs = System.currentTimeMillis();
    }

    public void start_action() {
        mMyAura = mMyBattler.doAction();
        mEnemyAura = mEnemyBattler.doAction();
        mScene = BattleScene.kAction;
        mStartMs = System.currentTimeMillis();
        echo("M=" + mMyBattler.getAction() + (mMyAura!=null ? "+"+mMyAura.getPower() : "") + " E=" + mEnemyBattler.getAction() + (mEnemyAura!=null ? "+"+mEnemyAura.getPower() : ""));
    }

    private void end_turn() {
        // 結果共有
        Battler.Status stE = mEnemyBattler.getStatus();
        Battler.Status stM = mMyBattler.getStatus();
        mMyBattler.notifyResult(mTurn, stE);
        mEnemyBattler.notifyResult(mTurn, stM);

        // オーラ消す
        mMyAura = null;
        mEnemyAura = null;

        if (stM.life > 0 && stE.life > 0) {
            // 次のターンへ
            mTurn++;
            echo("start turn: "+mTurn);
            mMyBattler.idling();
            mEnemyBattler.idling();
            mScene = BattleScene.kIdling;
        }
        else {
            // 決着
            if (stM.life <= 0) {
                echo("finish battle, You Lose!");
            }
            else {
                echo("finish battle, You Win!");
            }
            mMyBattler.finish();
            mEnemyBattler.finish();
            mScene = BattleScene.kFinish;
        }

        mStartMs = System.currentTimeMillis();
    }

    private void close_battle() {
        mScene = BattleScene.kClose;
        if (mListener != null) {
            mListener.onEnd();
        }
    }

    private double getElapsedSec() {
        return (System.currentTimeMillis() - mStartMs) / 1000.0;
    }

    private boolean isActive(Aura aura) {
        return (aura != null && aura.isActive());
    }

    public interface OnEndListener {
        public abstract void onEnd();
    }
    OnEndListener mListener;

    public void setOnEndListener(OnEndListener L) {
        mListener = L;
    }

    // デバッグ用
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
