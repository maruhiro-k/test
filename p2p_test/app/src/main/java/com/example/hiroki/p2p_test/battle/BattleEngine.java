package com.example.hiroki.p2p_test.battle;

import com.example.hiroki.p2p_test.battle.character.Aura;
import com.example.hiroki.p2p_test.battle.character.Player;
import com.example.hiroki.p2p_test.battle.protocol.BattleAction;
import com.example.hiroki.p2p_test.util.Logger;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
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
                        if (mEnemyPlayer.getAction() != BattleAction.DEFENCE || mMyAura.isSuper()) {
                            mEnemyPlayer.damage(+1);
                            echo("hit E.Life=" + mEnemyPlayer.getStatus().life);
                        }
                        else {
                            echo("guard E.Life=" + mEnemyPlayer.getStatus().life);
                        }
                        mMyAura.deactive();
                    } else if (!isActive(mMyAura) && isActive(mEnemyAura)) {
                        // 自分にヒット
                        if (mMyPlayer.getAction() != BattleAction.DEFENCE || mEnemyAura.isSuper()) {
                            mMyPlayer.damage(+1);
                            echo("hit M.Life=" + mMyPlayer.getStatus().life);
                        }
                        else {
                            echo("guard M.Life=" + mMyPlayer.getStatus().life);
                        }
                        mEnemyAura.deactive();
                    }
                }

                if (sec >= 3) {
                    // オーラ増減
                    if (mMyAura != null) {
                        mMyPlayer.changeAura(-mMyAura.getPower());
                        echo("down M.Aura=" + mMyPlayer.getStatus().aura);
                    } else if (mMyPlayer.getAction() == BattleAction.CHARGE) {
                        mMyPlayer.changeAura(+1);
                        echo("up M.Aura=" + mMyPlayer.getStatus().aura);
                    }
                    if (mEnemyAura != null) {
                        mEnemyPlayer.changeAura(-mEnemyAura.getPower());
                        echo("down E.Aura=" + mEnemyPlayer.getStatus().aura);
                    } else if (mEnemyPlayer.getAction() == BattleAction.CHARGE) {
                        mEnemyPlayer.changeAura(+1);
                        echo("up E.Aura=" + mEnemyPlayer.getStatus().aura);
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
        mMyAura = mMyPlayer.doAction();
        mEnemyAura = mEnemyPlayer.doAction();
        mScene = BattleScene.kAction;
        mStartMs = System.currentTimeMillis();
        echo("M=" + mMyPlayer.getAction() + (mMyAura!=null ? "+"+mMyAura.getPower() : "") + " E=" + mEnemyPlayer.getAction() + (mEnemyAura!=null ? "+"+mEnemyAura.getPower() : ""));
    }

    private void end_turn() {
        // 結果共有
        Player.Status stE = mEnemyPlayer.getStatus();
        Player.Status stM = mMyPlayer.getStatus();
        mMyPlayer.notifyResult(mTurn, stE);
        mEnemyPlayer.notifyResult(mTurn, stM);

        // オーラ消す
        mMyAura = null;
        mEnemyAura = null;

        if (stM.life > 0 && stE.life > 0) {
            // 次のターンへ
            mTurn++;
            echo("start turn: "+mTurn);
            mMyPlayer.idling();
            mEnemyPlayer.idling();
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
            mMyPlayer.finish();
            mEnemyPlayer.finish();
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
