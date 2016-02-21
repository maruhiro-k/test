package com.example.hiroki.p2p_test;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class BattleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        // 攻撃
        Button atk_btn = (Button) findViewById(R.id.attack_button);
        atk_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // オーラがあれば攻撃
                // オーラが満タンなら必殺
                // battle_engine.attack(aura);
            }
        });

        // 防御
        Button def_btn = (Button) findViewById(R.id.def_button);
        def_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 攻撃ならセーフ
                // 必殺ならアウト
                // battle_engine.defence();
            }
        });

        // ためる
        Button chg_btn = (Button) findViewById(R.id.charge_button);
        chg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // オーラ＋１
                // 無防備
                // battle_engine.charge();
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                will_back();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void will_back() {
        // 対戦中なら、戻る操作は確認する
        new AlertDialog.Builder(BattleActivity.this)
                .setMessage("対戦を終了しますか？\n負け扱いになります。")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 何もしない
                    }
                })
                .show();
    }
}
