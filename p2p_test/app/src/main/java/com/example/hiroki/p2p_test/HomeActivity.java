package com.example.hiroki.p2p_test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btn = (Button) findViewById(R.id.button2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, BattleActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("stop", "stop2");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("pause", "pause2");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("start", "start2");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("restart", "restart2");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("resume", "resume2");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("destroy", "destroy2");
    }
}
