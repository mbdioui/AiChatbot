package com.oab.orange.aichatbot;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.app.VoiceInteractor;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.widget.EditText;

public class ComposingMsgActivity extends AppCompatActivity {

    private EditText msg;
    private static final int PERMISSIONS_REQUEST_ALL_PERMISSIONS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.composing_msg_layout);
        msg= (EditText) findViewById(R.id.msg);
        if (!isVoiceInteractionRoot() || !isVoiceInteraction()) {
            //Not a voice interaction, proceed normally
            msg.setText("No voice interaction detected");
        } else if ( needPermissions(this))
            requestPermissions();

        else
        {
            beginVoiceInteraction();
        }
    }

    private void beginVoiceInteraction() {
        Intent intent=getIntent();
        String action = intent.getAction();
        if (action.equals(AlarmClock.ACTION_SET_ALARM)) {
            String hour = intent.getStringExtra(AlarmClock.EXTRA_HOUR);
            String minutes = intent.getStringExtra(AlarmClock.EXTRA_MINUTES);
            msg.setText("l\'horloge est mise à "+hour+" : "+minutes);
        }
    }
    static public boolean needPermissions(Activity activity) {
        Log.d("Test alarm voice interactions API", "needPermissions: ");
        return activity.checkSelfPermission(Manifest.permission.SET_ALARM)
                != PackageManager.PERMISSION_GRANTED
                || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        Log.d("Alarm permission", "requestPermissions: ");
        String[] permissions = new String[] {
                Manifest.permission.SET_ALARM,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        requestPermissions(permissions, PERMISSIONS_REQUEST_ALL_PERMISSIONS);
    }
}
