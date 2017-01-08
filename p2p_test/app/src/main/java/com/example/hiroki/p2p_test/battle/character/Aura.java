package com.example.hiroki.p2p_test.battle.character;

/**
 * Created by hiroki on 2016/03/21.
 */
public class Aura {
    static final int SUPER_POWER = 5;

    private int mPower;
    private boolean mIsActive = true;

    Aura(int power) {
        this.mPower = power;
    }

    public void deactive() {
        mIsActive = false;
    }

    public boolean isActive() {
        return mIsActive;
    }

    public boolean isSuper() {
        return (mPower >= SUPER_POWER);
    }

    public int getPower() {
        return mPower;
    }

    public void update() {
        // todo: 進む
    }
}
