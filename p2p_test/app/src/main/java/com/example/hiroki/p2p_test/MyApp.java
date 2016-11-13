package com.example.hiroki.p2p_test;

import android.app.Application;
import com.example.hiroki.p2p_test.lobby.RivalBase;

/**
 * Created by hiroki on 2016/04/03.
 */
public class MyApp extends Application {

    private RivalBase rival;

    public RivalBase getRival() {
        return rival;
    }

    public void setRival(RivalBase rival) {
        this.rival = rival;
    }

}
