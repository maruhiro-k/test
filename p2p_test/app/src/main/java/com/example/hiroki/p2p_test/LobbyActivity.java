package com.example.hiroki.p2p_test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class LobbyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Intent intent = getIntent();
        String user_name = intent.getStringExtra("user_name");
        Log.d("LobbyActivity", "name = " + user_name);

        // 検索
        Button search_btn = (Button) findViewById(R.id.search_button);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // WifiDirectで検索してリストアップ
            }
        });

        // 対戦
        Button battle_btn = (Button) findViewById(R.id.battle_button);
        battle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 選択していたユーザーと対戦開始
                // 選択していなかったら選択不可にする
                // 両者が開始を押した時点で開始

                Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
                // intent.putExtra("mydata", my_data);   // Serializableが便利っぽい
                // intent.putExtra("enemy", enemy_data);   // Serializableが便利っぽい
                startActivity(intent);
            }
        });
    }
}
