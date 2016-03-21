package com.example.hiroki.p2p_test.battle.character;

/**
 * Created by hiroki on 2016/03/21.
 */
public class Aura {
    static public final int SUPER_POWER = 5;

    int mPower;
    boolean mIsActive = true;

    public Aura(int power) {
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
