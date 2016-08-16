package com.example.hiroki.p2p_test.battle.controller;

import com.example.hiroki.p2p_test.battle.character.Player;

/**
 * Created by hiroki on 2016/03/21.
 */
public abstract class ControllerBase {
    private Listener mListener;
    protected boolean mLock = true;

    // 通知先を設定
    public ControllerBase() {
    }


    public void setListener(ControllerBase.Listener listener) {
        this.mListener = listener;
    }

    // 結果を受け取る
    public void setResult(int turn_number, final Player.Status my_data, final Player.Status enemy_data) {
    }

    // ターン開始
    public void startTurn() {
        mLock = false;
    }

    // 自分の行動
    protected void action(int act) {
        mLock = true;
        if (mListener != null) {
            mListener.action(this, act);
        }
    }

    // 相手の行動が決定した
    public void notifyDecideAction(int act) {
    }

    // 行動決定をコールバック
    public interface Listener{
        public void action(ControllerBase ctrl, int act);
    }
}
