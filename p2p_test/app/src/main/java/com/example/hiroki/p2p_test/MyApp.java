package com.example.hiroki.p2p_test;

import android.app.Application;

import com.example.hiroki.p2p_test.p2p.AsyncSocket;

/**
 * Created by hiroki on 2016/04/03.
 */
public class MyApp extends Application {

    private AsyncSocket s;

    public AsyncSocket getS() {
        return s;
    }

    public void setS(AsyncSocket s) {
        this.s = s;
    }

}