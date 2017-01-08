package com.example.hiroki.p2p_test.battle;

import com.example.hiroki.p2p_test.battle.character.Aura;
import com.example.hiroki.p2p_test.battle.character.Battler;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

/**
 * Created by hiroki on 2016/03/21.
 */
public class BattleEngine implements Battler.Listener {
    private int mTurn = 0;
    private Battler mMyBattler;
    private Battler mEnemyBattler;
    private Aura mMyAura;
    private Aura mEnemyAura;

    public int getTurn() {
        return mTurn;
    }

    private enum BattleScene {
        kOpen,
        kHello,
        kIdling,
        kAction,
        kFinish,
        kClose,
    }

    static public final int START_BATTLE    = 0x8000;   // 戦闘開始
    static public final int START_TURN      = 0x8001;   // ターン開始
    static public final int END_BATTLE      = 0x8002;   // 戦闘終了
    static public final int VANISHED        = 0x8003;   // 相殺
    static public final int DAMAGED         = 0x8004;   // 被弾
    static public final int NO_DAMAGED      = 0x8005;   // 被弾なし
    static public final int GIVEUP          = 0x8006;   // 降参
    static public final int CHARGED         = 0x8007;   // オーラ変化

    private BattleScene mScene;
    private Timer mTimer;
    private long mStartMs = 0;

    public BattleEngine(Battler my_battler, Battler enemy_battler) {
        mMyBattler = my_battler;
        mEnemyBattler = enemy_battler;
        mMyBattler.setListener(this);
        mEnemyBattler.setListener(this);
        mScene = BattleScene.kOpen;
    }

    public void start() {
        stop(); // いったん止める

        mListener.onAct(null, START_BATTLE);
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

                if (sec >= 0.5) {
                    if (isActive(mMyAura) && isActive(mEnemyAura)) {
                        // 相殺
                        mMyAura.deactive();
                        mEnemyAura.deactive();
                        mListener.onAct(null, VANISHED);
                    }
                }

                if (sec >= 1) {
                    if (isActive(mMyAura) && !isActive(mEnemyAura)) {
                        // 相手にヒット
                        if (mEnemyBattler.getAction() != BattleAction.DEFENCE || mMyAura.isSuper()) {
                            mEnemyBattler.damage(+1);
                            mListener.onAct(mEnemyBattler, DAMAGED);
                        }
                        else {
                            mListener.onAct(mEnemyBattler, NO_DAMAGED);
                        }
                        mMyAura.deactive();
                    } else if (!isActive(mMyAura) && isActive(mEnemyAura)) {
                        // 自分にヒット
                        if (mMyBattler.getAction() != BattleAction.DEFENCE || mEnemyAura.isSuper()) {
                            mMyBattler.damage(+1);
                            mListener.onAct(mMyBattler, DAMAGED);
                        }
                        else {
                            mListener.onAct(mMyBattler, NO_DAMAGED);
                        }
                        mEnemyAura.deactive();
                    }
                }

                if (sec >= 1.5) {
                    // オーラ増減
                    if (mMyAura != null) {
                        mMyBattler.changeAura(-mMyAura.getPower());
                    } else if (mMyBattler.getAction() == BattleAction.CHARGE) {
                        mMyBattler.changeAura(+1);
                        mListener.onAct(mMyBattler, CHARGED);
                    }
                    if (mEnemyAura != null) {
                        mEnemyBattler.changeAura(-mEnemyAura.getPower());
                    } else if (mEnemyBattler.getAction() == BattleAction.CHARGE) {
                        mEnemyBattler.changeAura(+1);
                        mListener.onAct(mEnemyBattler, CHARGED);
                    }

                    // アクション終了
                    end_turn();
                }
                break;
            case kFinish:
                if (getElapsedSec() >= 0.5) {
                    if (mTurn > 0) {
                        mListener.onAct(null, END_BATTLE);
                        mTurn = 0;
                    }
                }
                if (getElapsedSec() >= 3) {
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
        boolean isDecided = false;
        if (battler == mMyBattler) {
            isDecided = (mEnemyBattler.getAction() != BattleAction.NO_ACTION);
            mListener.onAct(mMyBattler, act);
            mEnemyBattler.notifyEnemyAction(act);
        }
        else if (battler == mEnemyBattler) {
            isDecided = (mMyBattler.getAction() != BattleAction.NO_ACTION);
            mMyBattler.notifyEnemyAction(act);
        }

        if (isDecided) {
            mListener.onAct(mEnemyBattler, mEnemyBattler.getAction());
            start_action();
        }
    }

    private void start_hello() {
        mMyBattler.hello();
        mEnemyBattler.hello();
        mScene = BattleScene.kHello;
        mTurn = 0;
        mStartMs = System.currentTimeMillis();
    }

    private void start_action() {
        mMyAura = mMyBattler.doAction();
        mEnemyAura = mEnemyBattler.doAction();
        mScene = BattleScene.kAction;
        mStartMs = System.currentTimeMillis();
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
            mListener.onAct(null, START_TURN);
            mMyBattler.idling();
            mEnemyBattler.idling();
            mScene = BattleScene.kIdling;
        }
        else {
            // 決着
            if (stM.life <= 0) {
                mListener.onAct(mMyBattler, GIVEUP);
            }
            else {
                mListener.onAct(mEnemyBattler, GIVEUP);
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

    public interface Listener {
        void onAct(Battler battler, int action);
        void onEnd();
    }
    private Listener mListener;

    public void setListener(Listener listener) {
        mListener = listener;
    }
}
