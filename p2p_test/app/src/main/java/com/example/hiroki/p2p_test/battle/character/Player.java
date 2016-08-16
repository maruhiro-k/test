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
        mAction = BattleAction.NO_ACTION;
    }
    public void idling() {
        // todo
        mAction = BattleAction.NO_ACTION;
        mCtrl.startTurn();
    }
    public void finish() {
        // todo
    }
    public Aura action() {
        // todo
        return null;
    }

    public void update() {
        // todo
//        step++;
        updateImpl();
    }

    protected void updateImpl() {
    }

    public void damage(int num) {
        mStatus.life -= num;
        // todo
        // damageAction();
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

    public Status getStatus() {
        return mStatus.clone();
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
