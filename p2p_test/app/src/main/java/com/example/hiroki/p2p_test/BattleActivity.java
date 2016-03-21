package com.example.hiroki.p2p_test;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.zip.CRC32;

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

        FrameLayout container = (FrameLayout) findViewById(R.id.battle_container);
        container.addView(new BattleView(this));
    }
    class BattleView extends SurfaceView implements SurfaceHolder.Callback {
        Bitmap b1;
        Bitmap b2;
        Bitmap b3;
        Matrix mat = new Matrix();

        BattleView(Context c) {
            super(c);
            getHolder().addCallback(this);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inScaled = false;
            b1 = BitmapFactory.decodeResource(getResources(), R.mipmap.player_normal, opt);
            b2 = BitmapFactory.decodeResource(getResources(), R.drawable.player_normal2, opt);

            mat.setScale(-1, -1);
            b3 = Bitmap.createBitmap(b2, 0, 0, 32, 32);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d("BattleView", "surfaceCreated");
            Canvas canvas = holder.lockCanvas();
//            canvas.scale(10, 10);
            doDraw(canvas);
            holder.unlockCanvasAndPost(canvas);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d("BattleView", "surfaceChanged");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d("BattleView", "surfaceDestroyed");
        }

        protected void doDraw(Canvas canvas) {
            Log.d("BattleView", "onDraw");
            canvas.drawColor(Color.BLACK);
            Paint p = new Paint();
            p.setColor(Color.CYAN);
            canvas.drawRect(10, 10, 100, 100, p);

            /*
            p.setAntiAlias(false);
            canvas.drawBitmap(b1, new Rect(0, 0, 32, 32), new Rect(0, 0, 320, 320), p);
        */
        }
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
