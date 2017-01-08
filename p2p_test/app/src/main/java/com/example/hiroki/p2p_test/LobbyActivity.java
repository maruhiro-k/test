package com.example.hiroki.p2p_test;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hiroki.p2p_test.lobby.p2p.WiFiDirectBroadcastReceiver;
import com.example.hiroki.p2p_test.lobby.rival.RandomRival;
import com.example.hiroki.p2p_test.lobby.rival.RivalBase;
import com.example.hiroki.p2p_test.lobby.rival.SocketTester;
import com.example.hiroki.p2p_test.lobby.rival.WifiRival;

import java.util.ArrayList;
import java.util.Collection;

public class LobbyActivity extends AppCompatActivity {
    WiFiDirectBroadcastReceiver mReceiver;
    MatchingListAdapter mListAdapter;
    AlertDialog mMatchingDlg;
    String mPlayerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Intent intent = getIntent();
        mPlayerName = intent.getStringExtra("name");
        Log.d("LobbyActivity", "name = " + mPlayerName);

        // 名前表示
        TextView nameText = (TextView) findViewById(R.id.name_text);
        nameText.setText(mPlayerName); // 名前が長いとはみ出すけど・・・（文字数制限でどうにか？）
        // 長い場合はフォントサイズを縮めてもいい？

        //  リスト
        mListAdapter = new MatchingListAdapter(this);
        ListView matching_list = (ListView) findViewById(R.id.enemy_list);
        matching_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        matching_list.setAdapter(mListAdapter);

        // WifiDirectで検索してリストアップ
        mReceiver = new WiFiDirectBroadcastReceiver(LobbyActivity.this, new WiFiDirectBroadcastReceiver.SearchListener() {
            @Override
            public void onSearch(Collection<WifiP2pDevice> devices) {
                onFoundDevices(devices);
            }
        });
        mReceiver.start();

        // 検索
        Button search_btn = (Button) findViewById(R.id.search_button);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 読み込み中の表示があったほうがいいかも？
                // 検索ボタンは無効にする？
                mReceiver.search();
            }
        });

        // 初期化
        onFoundDevices(null);
    }

    private void onFoundDevices(Collection<WifiP2pDevice> devices) {
        // リスト書き換え
        mListAdapter.clear();

        if (devices != null) {
            for (WifiP2pDevice device : devices) {
                mListAdapter.add(new WifiRival(device, mReceiver));
            }
        }
        mListAdapter.add(new RandomRival());
        mListAdapter.add(new SocketTester());

        mListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        stopMatching();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null) {
            mReceiver.close();
            mReceiver = null;
        }
        super.onDestroy();
    }

    private class MatchingListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        ArrayList<RivalBase> mRivals = new ArrayList<>();

        MatchingListAdapter(Context c) {
            mInflater = LayoutInflater.from(c);
        }

        void clear() {
            mRivals.clear();
        }

        void add(RivalBase rival) {
            mRivals.add(rival);
        }

        @Override
        public int getCount() {
            return mRivals.size();
        }

        @Override
        public Object getItem(int position) {
            return mRivals.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listitem_matching, null);
                // ボタンを置いたらハイライトしなくなるっぽい
            }

            final RivalBase rival = mRivals.get(position);
            TextView device_name = (TextView) convertView.findViewById(R.id.device_name_text);
            device_name.setText(rival.getName());

            // 対戦開始
            Button battle_btn = (Button) convertView.findViewById(R.id.battle_button);
            battle_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean ok = rival.requestBattle(mPlayerName);

                    if (ok) {
                        // 両者が開始を押した時点で開始
                        mMatchingDlg = new AlertDialog.Builder(LobbyActivity.this)
                                .setTitle("相手からの返事を待っています...")
                                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        rival.cancelBattle();
                                    }
                                })
                                .show();
                    }
                }
            });

            // リスナー設定
            rival.setListener(new RivalBase.Listener() {
                @Override
                public void onRecvCancel() {
                    // 要求が取り下げられたらリストの状態を変更！
                    // チカチカさせて、ボタンの名前を変更
                    stopMatching();
                    new AlertDialog.Builder(LobbyActivity.this)
                            .setTitle("キャンセルされました")
                            .show();
                }

                @Override
                public void onReadyCompleted() {
                    // つながったら対戦開始
                    stopMatching();
                    startBattle(rival);
                }
                @Override
                public void test(String memo) {
                    Toast.makeText(LobbyActivity.this, "shakeHand:"+memo, Toast.LENGTH_LONG).show();
                }
            });

            return convertView;
        }
    }

    private void startBattle(RivalBase enemy) {
        Log.d("test", "start battle: " + (enemy!=null ? enemy.toString() : "null"));
        // 対戦相手をセット
        MyApp appState = (MyApp) getApplicationContext();
        appState.setRival(enemy);

        startActivity(new Intent(appState, BattleActivity.class));
    }

    private void stopMatching() {
        if (mMatchingDlg != null) {
            mMatchingDlg.dismiss();
            mMatchingDlg = null;
        }
    }
}
