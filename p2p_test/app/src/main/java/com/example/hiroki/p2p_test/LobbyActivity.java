package com.example.hiroki.p2p_test;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.hiroki.p2p_test.battle.character.Player;
import com.example.hiroki.p2p_test.p2p.AsyncSocket;
import com.example.hiroki.p2p_test.p2p.WiFiDirectBroadcastReceiver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class LobbyActivity extends AppCompatActivity {
    WiFiDirectBroadcastReceiver mReceiver;
    MatchingListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        /*
        // 戦闘シーンだけテスト
        Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
        // intent.putExtra("mydata", my_data);   // Serializableが便利っぽい
        // intent.putExtra("enemy", enemy_data);   // Serializableが便利っぽい
        名前
        コントローラの種
        intent.putExtra("me", me);   // Serializableが便利っぽい
        intent.putExtra("enemy", enemy);   // Serializableが便利っぽい

        startActivity(intent);
*/
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        Log.d("LobbyActivity", "name = " + name);

        // 名前表示
        TextView nameText = (TextView) findViewById(R.id.name_text);
        nameText.setText(name); // 名前が長いとはみ出すけど・・・（文字数制限でどうにか？）
        // 長い場合はフォントサイズを縮めてもいい？

        //  リスト
        mListAdapter = new MatchingListAdapter(this);
        ListView matching_list = (ListView) findViewById(R.id.enemy_list);
        matching_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        matching_list.setAdapter(mListAdapter);

        matching_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("test", "onSelected: " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("test", "onNothingSelected");
            }
        });
        matching_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("test", "onClick: " + position);
            }
        });

        // WifiDirectで検索してリストアップ
        mReceiver = new WiFiDirectBroadcastReceiver(LobbyActivity.this, new WiFiDirectBroadcastReceiver.WfdListener() {
            @Override
            public void onSearch(Collection<WifiP2pDevice> devices) {
                onFoundDevices(devices);
            }

            @Override
            public void onConnect(AsyncSocket s) {
                // つながったら対戦開始
                // その前に情報交換確認かな

                Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
                // intent.putExtra("mydata", my_data);   // Serializableが便利っぽい
                // intent.putExtra("enemy", enemy_data);   // Serializableが便利っぽい

                MyApp appState = (MyApp)getApplicationContext();
                appState.setS(s);
                startActivity(intent);
            }

            @Override
            public void onDisconnect() {
            }
        }, null);
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
            Iterator<WifiP2pDevice> it2 = devices.iterator();
            while (it2.hasNext()) {
                mListAdapter.add(it2.next());
            }
        }
        mListAdapter.notifyDataSetChanged();
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
        ArrayList<WifiP2pDevice> mDevices = new ArrayList<WifiP2pDevice>();

        public MatchingListAdapter(Context c) {
            mInflater = LayoutInflater.from(c);
        }

        public void clear() {
            mDevices.clear();
        }

        public void add(WifiP2pDevice device) {
            mDevices.add(device);
/*
            // 表示確認のための水増し
            for (int i=0; i<10; ++i) {
                WifiP2pDevice d = new WifiP2pDevice();
                d.deviceName = device.deviceName + "." + Integer.toString(i);
                mDevices.add(d);
            }*/
        }

        @Override
        public int getCount() {
            return mDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return mDevices.get(position);
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

            final WifiP2pDevice dev = mDevices.get(position);
            TextView device_name = (TextView) convertView.findViewById(R.id.device_name_text);
            device_name.setText(dev.deviceName);

            // 対戦開始
            Button battle_btn = (Button) convertView.findViewById(R.id.battle_button);
            battle_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("test", "start battle: " + dev.deviceName);
                    mReceiver.connect(dev);

                    // 両者が開始を押した時点で開始
                    AlertDialog dlg = new AlertDialog.Builder(LobbyActivity.this)
                            .setTitle("相手からの返事を待っています...")
                            .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // todo: 確認すべき
                                    mReceiver.disconnect();
                                }
                            })
                            .show();

                    // todo: 接続できたり、切れたりしたらダイアログ閉じる
                    // dlg.dismiss();
                }
            });

            return convertView;
        }
    }
}
