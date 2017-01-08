package com.example.hiroki.p2p_test.battle.controller;

import java.util.Random;

/**
 * Created by hiroki on 2016/03/21.
 */
public class RandomController extends ControllerBase {
    private Random r;

    public RandomController(long seed) {
        r = new Random(seed);
    }

    @Override
    public void startTurn() {
        super.startTurn();
    }

    @Override
    public void notifyDecideAction(int act) {
        action(r.nextInt(4) + 1);
    }
}
