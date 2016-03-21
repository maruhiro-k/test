package com.example.hiroki.p2p_test.battle.controller;

import com.example.hiroki.p2p_test.battle.character.Player;

/**
 * Created by hiroki on 2016/03/21.
 */
public abstract class ControllerBase {
    private Listener mListener;
    protected boolean mLock = true;

    // 通知先を設定
    public ControllerBase(ControllerBase.Listener listener)
    {
        this.mListener = listener;
    }

    // 結果を受け取る
    public void setResult(int turn_number, Player.Status my_data, Player.Status enemy_data)
    {
        // win
        if (enemy_data.life == 0) {
            if (mListener != null) {
                mListener.onWin();
            }
        }
        // lose
        else if (my_data.life == 0) {
            if (mListener != null) {
                mListener.onLose();
            }
        }
        // next
        else {
            mLock = false;
            begin_turn(turn_number, my_data, enemy_data);
        }
    }

    // 自分の行動
    protected void action(int act) {
        mLock = true;
        end_turn(act);
        if (mListener != null) {
            mListener.action(this, act);
        }
    }

    // 相手の行動が決定した
    public void notifyDecideAction(int act) {
    }

    // ターン開始
    protected abstract void begin_turn(int turn_number, Player.Status my_data, Player.Status enemy_data);

    // ターン終了
    protected abstract void end_turn(int act);

    // 行動決定をコールバック
    public interface Listener{
        public void action(ControllerBase ctrl, int act);
        public void onWin();
        public void onLose();
    }
}
