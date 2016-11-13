package com.example.hiroki.p2p_test.lobby;

import android.os.AsyncTask;

import com.example.hiroki.p2p_test.battle.character.Battler;
import com.example.hiroki.p2p_test.battle.controller.ControllerBase;
import com.example.hiroki.p2p_test.battle.controller.RandomController;

/**
 * Created by hiroki on 2016/11/06.
 * CPUの対戦相手
 */

public class RandomRival extends RivalBase {
    public RandomRival() {
        super("ランダムさん");
    }

    @Override
    public boolean requestBattle(String name) {
        if (getStatus() == ConnectionStatus.SEND_REQUEST) {
            return true;    // 送信済み
        }
        if (getStatus() == ConnectionStatus.ALL_OK) {
            return true;    // 合意済み
        }
        setStatus(ConnectionStatus.SEND_REQUEST);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                setStatus(ConnectionStatus.ALL_OK);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return true;
    }

    @Override
    public ControllerBase createController() {
        return new RandomController(System.currentTimeMillis());
    }
}
