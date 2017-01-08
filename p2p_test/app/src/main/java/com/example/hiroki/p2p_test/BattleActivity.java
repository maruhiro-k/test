package com.example.hiroki.p2p_test;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.hiroki.p2p_test.battle.BattleEngine;
import com.example.hiroki.p2p_test.battle.character.Battler;
import com.example.hiroki.p2p_test.battle.controller.ButtonController;
import com.example.hiroki.p2p_test.lobby.rival.RivalBase;

import java.util.ArrayList;

import static com.example.hiroki.p2p_test.battle.BattleAction.ATTACK;
import static com.example.hiroki.p2p_test.battle.BattleAction.CHARGE;
import static com.example.hiroki.p2p_test.battle.BattleAction.DEFENCE;
import static com.example.hiroki.p2p_test.battle.BattleAction.NO_ACTION;
import static com.example.hiroki.p2p_test.battle.BattleAction.RAZZ;
import static com.example.hiroki.p2p_test.battle.BattleEngine.CHARGED;
import static com.example.hiroki.p2p_test.battle.BattleEngine.DAMAGED;
import static com.example.hiroki.p2p_test.battle.BattleEngine.END_BATTLE;
import static com.example.hiroki.p2p_test.battle.BattleEngine.GIVEUP;
import static com.example.hiroki.p2p_test.battle.BattleEngine.NO_DAMAGED;
import static com.example.hiroki.p2p_test.battle.BattleEngine.START_BATTLE;
import static com.example.hiroki.p2p_test.battle.BattleEngine.START_TURN;
import static com.example.hiroki.p2p_test.battle.BattleEngine.VANISHED;

public class BattleActivity extends AppCompatActivity {
    BattleEngine B;
    private BattleView mBattleView;

    public enum ActionOwner {
        kMyAction,          // 自分
        kEnemyAction,       // 敵
    }

    public class ActionLog {
        ActionOwner owner;
        String text;
        int width, height;

        ActionLog(ActionOwner owner, String text) {
            this.owner = owner;
            this.text = text;
            this.width = 0;
            this.height = 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        FrameLayout container = (FrameLayout) findViewById(R.id.battle_container);
        mBattleView = new BattleView(this);
        container.addView(mBattleView);

        // 自分を生成
        Button buttons[] = new Button[3];
        buttons[0] = (Button) findViewById(R.id.attack_btn);
        buttons[1] = (Button) findViewById(R.id.guard_btn);
        buttons[2] = (Button) findViewById(R.id.power_btn);
        final Battler me = new Battler("me", new ButtonController(buttons));

        // 敵を取り出す
        MyApp appState = (MyApp)getApplicationContext();
        RivalBase rival = appState.getRival();
        final Battler enemy = new Battler(rival.getName(), rival.createController());

        // 対戦用意
        B = new BattleEngine(me, enemy);
        B.setListener(new BattleEngine.Listener(){
            @Override
            public void onAct(Battler battler, int action) {
                ActionOwner owner = null;
                String text = null;
                switch (action) {
                    case START_BATTLE:
                        text = "バトルスタート！";
                        break;
                    case START_TURN:
                        text = B.getTurn() + "ターン";
                        break;
                    case END_BATTLE:
                        if (me.getStatus().life > 0) {
                            text = " Y O U  W I N ！";
                        } else {
                            text = " Y O U  L O S E ！";
                        }
                        break;
                    case VANISHED:
                        text = "相殺！";
                        break;

                    // 以下、Battlerごとのセリフ
                    // Battlerの性格パターン作って多態したい
                    case NO_ACTION:
                        text = "何もしない！";
                        break;
                    case ATTACK:
                        text = "攻撃！";
                        break;
                    case DEFENCE:
                        text = "防御！";
                        break;
                    case CHARGE:
                        text = "チャージ！";
                        break;
                    case RAZZ:
                        text = "挑発！";
                        break;
                    case CHARGED:
                        // text = "オーラ " + battler.getStatus().aura + "！";
                        // 黙ってる
                        break;
                    case DAMAGED:
                        text = "いてえ！";
                        break;
                    case NO_DAMAGED:
                        text = "きかぬわ！";
                        break;
                    case GIVEUP:
                        text = "まいった！";
                        break;
                }
                if (text != null) {
                    if (battler == me) {
                        owner = ActionOwner.kMyAction;
                    }
                    else if (battler == enemy) {
                        owner = ActionOwner.kEnemyAction;
                    }
                    mBattleView.update(new ActionLog(owner, text));
                }
            }

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

        B.start();
    }

    class BattleView extends SurfaceView implements SurfaceHolder.Callback {
        Bitmap mMyFrame;
        Bitmap mEnemyFrame;
        Bitmap mSystemFrame;

        BattleView(Context c) {
            super(c);
            getHolder().setFormat(PixelFormat.TRANSLUCENT);
            getHolder().addCallback(this);
            setZOrderOnTop(true);

            // 画像読み込み
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inScaled = false;
            Bitmap frames = BitmapFactory.decodeResource(getResources(), R.drawable.frame, opt);

            mMyFrame = Bitmap.createBitmap(frames, 0, 0, 128, 128);
            mEnemyFrame = Bitmap.createBitmap(frames, 128, 0, 128, 128);
            mSystemFrame = Bitmap.createBitmap(frames, 0, 128, 128, 128);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            doDraw(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            doDraw(holder);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }

        protected void doDraw(SurfaceHolder holder) {
            Canvas canvas = holder.lockCanvas();
            if (canvas == null) {
                return;
            }

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            if (mActionLogs != null && ! mActionLogs.isEmpty()) {
                final int FRAME_W = 60;
                final int FRAME_H = 120;

                int cw = canvas.getWidth();

                TextPaint textPaint = new TextPaint();
                textPaint.setTextSize(48.f);
                textPaint.setTypeface(Typeface.SANS_SERIF);

                // 高さを最初に計算
                int y0 = 0;
                for (ActionLog act : mActionLogs) {
                    if (act.width == 0) {
                        act.width = (int)Math.ceil(textPaint.measureText(act.text));
                        act.height = FRAME_H;
                        if (act.owner == null) {
                            act.height -= 16;
                        }
                    }
                    y0 += act.height;
                }

                int maxHeight = canvas.getHeight();
                if (y0 > maxHeight) {
                    y0 = maxHeight;
                }

                // 下から並べる
                for (int i=mActionLogs.size()-1; i>=0; i--) {
                    ActionLog act = mActionLogs.get(i);

                    int x0 = 0;
                    Bitmap bmp;
                    if (act.owner == ActionOwner.kMyAction) {
                        bmp = mMyFrame;
                    }
                    else if (act.owner == ActionOwner.kEnemyAction) {
                        bmp = mEnemyFrame;
                        x0 = cw - FRAME_W*2 - act.width;
                    }
                    else {
                        bmp = mSystemFrame;
                        x0 = (cw - act.width) / 2 - FRAME_W;
                    }
                    y0 -= act.height;

                    canvas.save();
                    canvas.translate(x0, y0);
                    canvas.drawBitmap(bmp, new Rect(0, 0, FRAME_W, FRAME_H), new Rect(0, 0, FRAME_W, FRAME_H), null);
                    canvas.drawBitmap(bmp, new Rect(FRAME_W, 0, FRAME_W+8, FRAME_H), new Rect(FRAME_W, 0, FRAME_W+act.width, FRAME_H), null);
                    canvas.drawBitmap(bmp, new Rect(FRAME_W+8, 0, FRAME_W*2+8, FRAME_H), new Rect(FRAME_W+act.width, 0, FRAME_W*2+act.width, FRAME_H), null);
                    canvas.drawText(act.text, FRAME_W, (int)(act.height*0.6), textPaint);
                    canvas.restore();
                }
            }

            holder.unlockCanvasAndPost(canvas);
        }

        public void update(ActionLog log) {
            mActionLogs.add(log);
            doDraw(getHolder());
        }

        ArrayList<ActionLog> mActionLogs = new ArrayList<>();
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
        if (B.getTurn() <= 0) {
            finish();
            return;
        }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 敵を取り出す
        MyApp appState = (MyApp)getApplicationContext();
        RivalBase rival = appState.getRival();
        rival.cancelBattle();
    }
}
