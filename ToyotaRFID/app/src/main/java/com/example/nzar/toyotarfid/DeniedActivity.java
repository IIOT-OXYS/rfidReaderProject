//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

/*
This activity is where the user is placed if their ID does not match the training required for the device.
If the users ID is not found in the database, they will also land there.
 */

public class DeniedActivity extends AppCompatActivity implements View.OnClickListener {

    private final int TIMEOUT = 300000;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setContentView(R.layout.activity_denied);
        //set up button click listeners
        Button ret = (Button) findViewById(R.id.UnauthorizedReturnButton);
        ret.setOnClickListener(this);
        Button Contact = (Button) findViewById(R.id.Contact);
        Contact.setOnClickListener(this);
        //show date
        TextView dateText = (TextView) findViewById(R.id.TextDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        dateText.setText(dateFormat.format(Calendar.getInstance().getTime()));

        startTimer();
    }

    // blackout screen after 5 minutes
    private void startTimer(){
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                logout();
            }
        };
        timer.schedule(timerTask, TIMEOUT);
    }

    // cleanup and return to main screen
    private void logout() {
        timer.cancel();
        DatabaseConnector.TILTPostUserTask Job = new DatabaseConnector.TILTPostUserTask();
        Job.setLoggingOut(true);
        Job.setSessionID(DatabaseConnector.currentSessionID);
        Job.execute(DatabaseConnector.currentBadgeID);
        Intent x = new Intent(this, MainActivity.class);
        startActivity(x);
    }

    // handle user touch
    @Override
    public void onClick(View v) {
        //switch to navigate based on button pressed
        switch (v.getId()) {
            case R.id.UnauthorizedReturnButton:
                logout();
                break;
            case R.id.Contact:
                timer.cancel();
                Intent contact = new Intent(DeniedActivity.this, TechContact.class);
                contact.putExtra("return", "DeniedActivity");
                DeniedActivity.this.startActivity(contact);
                break;
        }
    }

    //  ensure activity stops cleanly
    @Override
    protected void onStop() {
        timer.cancel();
        timer.purge();
        super.onStop();
    }


}



