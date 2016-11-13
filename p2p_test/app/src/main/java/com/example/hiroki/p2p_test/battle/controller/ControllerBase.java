package com.example.hiroki.p2p_test.battle.controller;

import com.example.hiroki.p2p_test.battle.character.Battler;

/**
 * Created by hiroki on 2016/03/21.
 */
public abstract class ControllerBase {
    private Listener mListener;
    private boolean mLock = true;

    // 通知先を設定
    ControllerBase() {
    }


    public void setListener(ControllerBase.Listener listener) {
        this.mListener = listener;
    }

    // 結果を受け取る
    public void setResult(int turn_number, final Battler.Status my_data, final Battler.Status enemy_data) {
    }

    // ターン開始
    public void startTurn() {
        lock(false);
    }

    // 自分の行動
    protected void action(int act) {
        lock(true);
        if (mListener != null) {
            mListener.action(this, act);
        }
    }

    // 操作不可を切り替え
    public void lock(boolean is_lock) {
        mLock = is_lock;
    }

    // 相手の行動が決定した
    public void notifyDecideAction(int act) {
    }

    // 行動決定をコールバック
    public interface Listener{
        public void action(ControllerBase ctrl, int act);
    }
}
