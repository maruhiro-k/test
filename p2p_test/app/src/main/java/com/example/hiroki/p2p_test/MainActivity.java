package com.example.hiroki.p2p_test;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // セーブデータ確認
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String name = pref.getString("name", "");

        // 名前なしなら入力させる
        if (name.isEmpty()) {
            final EditText text = new EditText(this);
            text.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME);
            text.setHint("*文字まで");
            // todo: 文字数制限したい

            new AlertDialog.Builder(this)
                    .setTitle("名前を入力してください")
                    .setView(text)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 名前決定
                            goToLobby(text.getText().toString());
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
        else {
            // 名前が決まっているならすぐに次の画面へ
            goToLobby(name);
        }

        /*
        for test
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button sock_btn = (Button) findViewById(R.id.sock_btn);
        sock_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SockActivity.class);
                startActivity(intent);
                finish();
            }
        });
         */
    }

    private boolean setWifiOn() {
        WifiManager wifi = (WifiManager)getSystemService(WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            boolean res = wifi.setWifiEnabled(true);
            if (!res) {
                return false;
            }
        }
        return true;
    }

    private void goToLobby(String name) {
        boolean wifi = setWifiOn();
        if (!wifi) {
            // エラー
            return;
        }

        // ロビーへ移動
        Log.d("test", "name = " + name);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref.getString("name", "").compareTo(name) != 0) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("name", name);
            editor.apply();
        }

        Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
        intent.putExtra("name", name);
        startActivity(intent);
        finish();   // ここに戻ることはないので閉じる
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("stop", "stop1");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("pause", "pause1");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("start", "start1");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("restart", "restart1");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("resume", "resume1");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("destroy", "destroy1");
    }
}
