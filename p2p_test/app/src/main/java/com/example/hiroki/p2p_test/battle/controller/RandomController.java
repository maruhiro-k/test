package com.example.hiroki.p2p_test.battle.controller;

import com.example.hiroki.p2p_test.battle.character.Player;
import com.example.hiroki.p2p_test.battle.protocol.BattleAction;

import java.util.Random;

/**
 * Created by hiroki on 2016/03/21.
 */
public class RandomController extends ControllerBase {
    Random r;

    public RandomController(long seed) {
        r = new Random(seed);
    }

    @Override
    public void startTurn() {
        super.startTurn();
        action(r.nextInt(4) + 1);
    }
}
