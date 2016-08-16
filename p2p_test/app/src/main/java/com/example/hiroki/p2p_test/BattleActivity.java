package com.example.hiroki.p2p_test;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.hiroki.p2p_test.battle.BattleEngine;
import com.example.hiroki.p2p_test.battle.character.Player;
import com.example.hiroki.p2p_test.battle.controller.ButtonController;
import com.example.hiroki.p2p_test.battle.controller.ControllerBase;
import com.example.hiroki.p2p_test.battle.controller.RandomController;
import com.example.hiroki.p2p_test.battle.controller.SocketController;
import com.example.hiroki.p2p_test.p2p.AsyncSocket;

public class BattleActivity extends AppCompatActivity {
    BattleEngine B;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        FrameLayout container = (FrameLayout) findViewById(R.id.battle_container);
        container.addView(new BattleView(this));

        // 自分を生成
        Button buttons[] = new Button[3];
        buttons[0] = (Button) findViewById(R.id.attack_btn);
        buttons[1] = (Button) findViewById(R.id.guard_btn);
        buttons[2] = (Button) findViewById(R.id.power_btn);
        Player me = new Player("me", new ButtonController(buttons));

        // 敵を生成
        MyApp appState = (MyApp)getApplicationContext();
        AsyncSocket s = appState.getSocket();
        ControllerBase ctrl;
        if (s!=null) {
            ctrl = new SocketController(s);
        }
        else {
            ctrl = new RandomController(System.currentTimeMillis());
        }
        Player enemy = new Player("enemy", ctrl);

        // 対戦用意
        B = new BattleEngine(me, enemy);
        B.start();
        B.setOnEndListener(new BattleEngine.OnEndListener(){
            @Override
            public void onEnd() {
                new AlertDialog.Builder(BattleActivity.this)
                        .setTitle("再戦しますか？")
                        .setPositiveButton("再戦！", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                B.start();
                            }
                        })
                        .setNegativeButton("終了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            }
        });

        // デバッグ用
        B.setLogView((TextView) findViewById(R.id.log_text));
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
