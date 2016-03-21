package com.example.hiroki.p2p_test.battle.controller;

import com.example.hiroki.p2p_test.battle.character.Player;
import com.example.hiroki.p2p_test.battle.protocol.BattleAction;

import java.util.Random;

/**
 * Created by hiroki on 2016/03/21.
 */
public class RandomController extends ControllerBase {
    Random r = new Random();

    public RandomController(int seed, ControllerBase.Listener listener) {
        super(listener);
    }

    @Override
    protected void begin_turn(int turn_number, Player.Status my_data, Player.Status enemy_data) {
        action(r.nextInt(4) + 1);
    }

    @Override
    protected void end_turn(int act) {
        // 何もしない
    }
}
