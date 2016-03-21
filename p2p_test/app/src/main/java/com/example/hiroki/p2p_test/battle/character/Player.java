package com.example.hiroki.p2p_test.battle.character;

import com.example.hiroki.p2p_test.battle.controller.ControllerBase;

/**
 * Created by hiroki on 2016/03/21.
 */
public class Player {
    String mName;
    Status mStatus;
    ControllerBase mCtrl;
//    Bitmap charImage[];

    public Player(String name) {
        this.mName = name;
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
    }
    public void idling() {
        // todo
    }
    public Aura action() {
        // todo
        return null;
    }

    public void update()
    {
        // todo
//        step++;
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
        mCtrl.setResult(turn_number, getStatus(), enemy_data);
        // todo: 送った結果どうするのか考え直す
    }

    public Status getStatus() {
        return mStatus.clone();
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
}
