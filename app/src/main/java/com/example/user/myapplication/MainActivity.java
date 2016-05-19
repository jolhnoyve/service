package com.example.user.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    private Button startButton;
    private Button stopButton;
    private TextView textView;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        textView = (TextView)findViewById(R.id.text);
        startButton.setOnClickListener(startClickListener);
        stopButton.setOnClickListener(stopClickListener);
    }

    private Button.OnClickListener startClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            //啟動服務
            Intent intent = new Intent(MainActivity.this,NickyService.class);
            intent.putExtra("User_APIKEY","RO28U1DJSWQB8Q3L");
            startService(intent);
        }
    };

    private Button.OnClickListener stopClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            //停止服務
            Intent intent = new Intent(MainActivity.this, NickyService.class);
            stopService(intent);
        }
    };
}
