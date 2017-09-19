package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

public class SecondaryBadgeActivity extends AppCompatActivity implements View.OnClickListener {

    private StringBuilder ID = new StringBuilder();
private final String TAG = "SecondaryBadgeIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary_badge);


        Button Cancel = (Button) findViewById(R.id.SecondaryBadgeInCancel);
        Button ContactTech = (Button) findViewById(R.id.SecondaryBadgeInContactTech);

        Cancel.setOnClickListener(this);
        ContactTech.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.SecondaryBadgeInCancel:
                new DatabaseConnector.TILTPostUserTask()
                        .execute(DatabaseConnector.currentBadgeID, String.valueOf(DatabaseConnector.currentSessionID));
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.SecondaryBadgeInContactTech:

                Intent contactTech = new Intent(this, TechContact.class);
                contactTech.putExtra("return", "SecondaryBadgeIn");
                startActivity(contactTech);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (ID.length() == 1) {//inform the user their tap was registered
            TextView tv = (TextView) findViewById(R.id.main_activity_text);
            tv.setText("Checking certifications. . .");
        }
        if (keyCode == KeyEvent.KEYCODE_BACKSLASH || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEMICOLON) {//checks for ascii delimiter
            Log.d(TAG, ID.toString().trim());//log ID for debugging
            String badgeNumber = ID.toString().trim(); // builds the string from the string builder
            DatabaseConnector.TILTPostUserTask Job = new DatabaseConnector.TILTPostUserTask();
            Job.execute(badgeNumber, String.valueOf(DatabaseConnector.currentSessionID));//execute the query on a separate thread

            try {
                String Authorization = Job.get();
                switch (Authorization) {
                    case "CheckPPE":
                        startActivity(new Intent(this, CheckActivity.class));
                        break;
                    default:
                        startActivity(new Intent(this, DeniedActivity.class));
                        break;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                ID.delete(0,ID.length());
                Toast.makeText(this, "Couldn't contact API server for certifications", Toast.LENGTH_LONG).show();
            }


        } else {//delimeter not detected, log input and proceed
            char c = (char) event.getUnicodeChar();
            ID.append(c);
        }
        return super.onKeyDown(keyCode, event);
    }
}

