package com.example.hiroki.p2p_test.battle.character;

import android.util.Log;

import com.example.hiroki.p2p_test.battle.controller.ControllerBase;
import com.example.hiroki.p2p_test.battle.protocol.BattleAction;

/**
 * Created by hiroki on 2016/03/21.
 */
public class Player implements ControllerBase.Listener {
    String mName;
    Status mStatus;
    ControllerBase mCtrl;
//    Bitmap charImage[];

    Listener mListener;
    int mAction;

    public Player(String name, ControllerBase ctrl) {
        this.mName = name;
        this.mCtrl = ctrl;
        mCtrl.setListener(this);
        this.mStatus = new Status();
        mAction = BattleAction.NO_ACTION;
    }

    public void setListener(Player.Listener listener) {
        this.mListener = listener;
    }

    public void start()
    {
        // todo
//        hello();
    }
    // 挨拶
    // 待機
    // 攻撃
    // 防御
    // 貯め
    // 挑発
    // くらい
    // 勝利
    // 倒れる
    public void hello() {
        // todo
        // setPose(Hello);
        mStatus = new Status(); // 新しいものを常に生成
        mAction = BattleAction.NO_ACTION;
    }
    public void idling() {
        // todo
        // setPose(Idling);
        mAction = BattleAction.NO_ACTION;
        mCtrl.startTurn();
    }
    public void finish() {
        // todo
        // setPose(win or lose);
    }
    public Aura doAction() {
        mCtrl.lock(true);   // 念のためここでもロック

        // todo 姿勢を変える
        // setPose(mAction);

        // 攻撃
        if (mAction == BattleAction.ATTACK) {
            if (mStatus.aura == 0) {
                return null;    // オーラ不足で不発
            }
            else if (mStatus.aura >= Aura.SUPER_POWER) {
                return new Aura(mStatus.aura);  // 超撃
            }
            else {
                return new Aura(1);
            }
        }

        return null;
    }

    public void damage(int num) {
        mStatus.life -= num;

        if (num > 0) {
            // todo
            // setPose(damaged);
        }
    }

    public void changeAura(int num) {
        mStatus.aura += num;
        if (mStatus.aura > Aura.SUPER_POWER) {
            mStatus.aura = Aura.SUPER_POWER;
        }
        else if (mStatus.aura < 0) {
            mStatus.aura = 0;
        }
        // todo
    }

    public void update() {
        // todo
        // 姿勢アニメーション
    }

    public void notifyEnemyAction(int act) {
    }

    public void notifyResult(int turn_number, Player.Status enemy_data) {
        if (enemy_data.life == 0) {
            // win
        }
        else if (mStatus.life == 0) {
            // lose
        }

        mCtrl.setResult(turn_number, mStatus, enemy_data);
    }

    public final Status getStatus() {
        return mStatus;
    }

    public int getAction() {
        return mAction;
    }

    @Override
    public void action(ControllerBase ctrl, int act) {
        mAction = act;
        Log.d(mName, "act=" + mAction);
        if (mListener != null) {
            mListener.action(this, act);
        }
    }

    static public class Status {
        public int life = 1;
        public int aura = 0;

        public Status clone() {
            Status s = new Status();
            s.life = life;
            s.aura = aura;
            return s;
        }
    }

    // 行動決定をコールバック
    public interface Listener{
        public void action(Player player, int act);
    }
}
