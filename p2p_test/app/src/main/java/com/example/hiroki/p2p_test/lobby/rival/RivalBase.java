package com.example.hiroki.p2p_test.lobby.rival;

import com.example.hiroki.p2p_test.battle.controller.ControllerBase;

/**
 * Created by hiroki on 2016/11/06.
 */

abstract public class RivalBase {
    private String  mRivalTag;

    RivalBase(String tag) {
        mRivalTag = tag;
        cancelBattle();
    }

    /**
     * @return 識別できるような名前を返す
     */
    final public String getName() {
        String name = getBattlerName();
        if (name == null) {
            name = mRivalTag;
        }
        if (name == null) {
            name = "名無し";
        }
        return name;
    }

    /**
     * 対戦者のバトルネームを返す
     * @return ユーザーを示す名前
     */
    public String getBattlerName() {
        return null;
    }

    /**
     * バトルの申し込みをする
     * @param name 自分の名前を渡す
     * @return 申請できたら true
     */
    public abstract boolean requestBattle(String name);

    /**
     * バトルのキャンセル
     */
    public abstract void cancelBattle();

    enum ConnectionStatus {
        DISCONNECTED,   // 未接続
        CONNECTED,      // 接続済み
        SEND_REQUEST,   // 申請した
        ALL_OK,         // 合意ができた
    }

    abstract ConnectionStatus getStatus();

    public interface Listener {
        void onRecvCancel();
        void onReadyCompleted();

        void test(String memo);
    }
    Listener mListener;

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    /**
     * 対戦状態の変化を反映
     * 申し込み状態が変化したときに適切によびだすこと
     */
    protected void dispatchBattleStatus() {
        if (mListener != null) {
            if (getStatus() == ConnectionStatus.ALL_OK) {
                mListener.onReadyCompleted();
            }
            else {
                mListener.onRecvCancel();
            }
        }
    }

    public abstract ControllerBase createController();
}
