package com.example.hiroki.p2p_test.battle.controller;

import android.view.View;
import android.widget.Button;

import com.example.hiroki.p2p_test.battle.character.Player;
import com.example.hiroki.p2p_test.battle.protocol.BattleAction;

/**
 * Created by hiroki on 2016/03/21.
 */
public class ButtonController extends ControllerBase {
    Button mButtons[] = new Button[5];

    public ButtonController(Button btns[]) {
        for (int i=0; i<btns.length; ++i) {
            registerButton(btns[i], i);
        }
    }

    private boolean registerButton(Button btn, final int act) {
        if (btn == null) {
            return false;
        }
        if (act < 0 || act >= mButtons.length) {
            return false;
        }

        mButtons[act] = btn;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action(act+1);
            }
        });
        return true;
    }

    @Override
    public void startTurn() {
        enableInput(true);
        super.startTurn();
    }

    @Override
    protected void action(int act) {
        enableInput(false);
        super.action(act);
    }

    private void enableInput(boolean enable) {
        for (Button b : mButtons) {
            if (b != null) {
                b.setEnabled(enable);
            }
        }
    }
}
