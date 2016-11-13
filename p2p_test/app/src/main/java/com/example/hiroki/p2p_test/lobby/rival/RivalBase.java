package com.example.hiroki.p2p_test.lobby.rival;

import com.example.hiroki.p2p_test.battle.controller.ControllerBase;

/**
 * Created by hiroki on 2016/11/06.
 */

abstract public class RivalBase {
    private String  mRivalTag;
    private ConnectionStatus mStatus;

    RivalBase(String tag) {
        mRivalTag = tag;
        mStatus = ConnectionStatus.DISCONNECTED;
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
    public void cancelBattle() {
    }

    enum ConnectionStatus {
        DISCONNECTED,   // 未接続
        CONNECTED,      // 接続済み
        SEND_REQUEST,   // 申請した
        ALL_OK,         // 合意ができた
    }

    ConnectionStatus getStatus() {
        return mStatus;
    }

    void setStatus(ConnectionStatus st) {
        if (mStatus == st) {
            return;
        }

        // キャンセル判定
        if (mStatus == ConnectionStatus.CONNECTED && st == ConnectionStatus.DISCONNECTED) {
            if (mListener != null) {
                mListener.onRecvCancel();
            }
        }
        if (mStatus == ConnectionStatus.SEND_REQUEST && st != ConnectionStatus.ALL_OK) {
            if (mListener != null) {
                mListener.onRecvCancel();
            }
        }

        mStatus = st;

        // リスナーにも投げる
        switch (mStatus) {
            case DISCONNECTED:
                break;
            case CONNECTED:
            case SEND_REQUEST:
                // 何もしない
                break;
            case ALL_OK:
                // 戦闘準備完了
                if (mListener != null) {
                    mListener.onReadyCompleted();
                }
                break;
        }
    }

    public interface Listener {
        void onRecvCancel();
        void onReadyCompleted();
    }
    Listener mListener;

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public abstract ControllerBase createController();
}