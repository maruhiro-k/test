package com.example.hiroki.p2p_test;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    AlertDialog dlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start_btn = (Button) findViewById(R.id.start_button);
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
                intent.putExtra("user_name", "test name");
                startActivity(intent);
                finish();   // ここに戻ることはないので閉じる
            }
        });
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
/*
        // input name
        final EditText editText = new EditText(MainActivity.this);

        this.dlg = new AlertDialog.Builder(MainActivity.this)
                .setMessage("メッセージ")
                .setPositiveButton("OKOK", new OKButtonClickHandler(editText))
                .setView(editText)
                .setCancelable(false)
                .create();

        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//                startActivity(intent);
                dlg.show();
            }
        });

        editText.setOnFocusChangeListener(new ForcusChangeHandler(this.dlg));

        EditText editText2 = (EditText) findViewById(R.id.editText);
        editText2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d("editor action", String.format("id %d", actionId));
                ((EditText)v).selectAll();
                return false;
            }
        });
*/
    }
    /*
    private class ForcusChangeHandler implements View.OnFocusChangeListener {        // (2)
        AlertDialog dialog;

        public ForcusChangeHandler(AlertDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {                   // (3)
            if (hasFocus) {
                dialog.getWindow().setSoftInputMode(                            // (4)
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }
    }
    private class OKButtonClickHandler implements DialogInterface.OnClickListener {
        EditText editText;

        public OKButtonClickHandler(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void onClick(DialogInterface dialog,
                            int which) {
            Log.d("input", String.format("text %s which %d %d %d", editText.getText().toString(), which, DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE));
        }
    }
*/
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
