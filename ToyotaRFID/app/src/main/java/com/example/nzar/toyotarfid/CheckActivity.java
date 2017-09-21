//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

public class CheckActivity extends AppCompatActivity implements View.OnClickListener {

    private static int PPECount = 0;
    private final String TAG = "CzechActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setContentView(R.layout.activity_check);

        //show date
        TextView dateText = (TextView) findViewById(R.id.TextDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        dateText.setText(dateFormat.format(Calendar.getInstance().getTime()));

        //creating all the buttons and toggle button
        Button yes = (Button) findViewById(R.id.Yes);
        yes.setOnClickListener(this);
        yes.setEnabled(false);
        final Button cancel = (Button) findViewById(R.id.CheckActivityCancelButton);
        cancel.setOnClickListener(this);
        final Button contact = (Button) findViewById(R.id.Contact);
        contact.setOnClickListener(this);

        HashMap<DatabaseConnector.PPE, Button> PPEButtons = new HashMap<>();
        HashMap<DatabaseConnector.PPE, TextView> PPETexts = new HashMap<>();

        generatePPELists(PPEButtons,PPETexts);
        for (DatabaseConnector.PPE ppe : PPEButtons.keySet()) {
            //for final implementation, there will be logic to decide if the button should be enabled
            //based on the database query
            Button PPEButton = PPEButtons.get(ppe);
            TextView PPEText = PPETexts.get(ppe);
            PPEButton.setVisibility(View.VISIBLE);
            if (ppe.Image != null) {
                PPEButton.setBackground(ppe.Image);
            }
            PPEButton.setOnClickListener(this);
            if (ppe.name != null) {
                PPEText.setText(ppe.name);
                PPEText.setVisibility(View.VISIBLE);
            }


        }
        PPECount = PPEButtons.size();
        Log.d(TAG, "Found " + String.valueOf(PPECount) + " PPEs");
        if (PPECount <= 0) {
            yes.setEnabled(true);
        } else {
            yes.setBackgroundColor(0x88659941);
        }

    }

    //This method is for programming all the buttons
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //starts the activity with timer
            case R.id.Yes:
                Intent i = new Intent(CheckActivity.this, TimeActivity.class);
                startActivity(i);
                break;
            //brings you back to the main screen (employee is not following PPE guidelines)
            case R.id.CheckActivityCancelButton:
                PPECount = 0;
                DatabaseConnector.TILTPostUserTask Job = new DatabaseConnector.TILTPostUserTask();
                Job.execute(DatabaseConnector.currentBadgeID, String.valueOf(DatabaseConnector.currentSessionID));
                Intent x = new Intent(CheckActivity.this, MainActivity.class);
                startActivity(x);
                break;
            //goes to the contact tech page
            case R.id.Contact:
                PPECount = 0;
                Intent contact = new Intent(CheckActivity.this, TechContact.class);
                contact.putExtra("return", "CheckActivity");
                startActivity(contact);
                break;
            default:
                if (findViewById(v.getId()).getVisibility() == View.VISIBLE) {
                    v.setBackgroundColor(Color.GREEN);
                    v.setEnabled(false);
                    PPECount--;
                    if (PPECount <= 0) {
                        PPECount = 0;
                        findViewById(R.id.Yes).setEnabled(true);
                        findViewById(R.id.Yes).setBackgroundColor(0xff659941);
                    }
                }
                break;
        }
    }

    //method for generating the ImageButtons used to continue to the TimeActivity
    private void generatePPELists(HashMap<DatabaseConnector.PPE, Button> PPEButtons, HashMap<DatabaseConnector.PPE, TextView> PPETexts) {
        ArrayList<Button> buttons = new ArrayList<>();
        ArrayList<TextView> texts = new ArrayList<>();

        buttons.add((Button) findViewById(R.id.PPE1));
        buttons.add((Button) findViewById(R.id.PPE2));
        buttons.add((Button) findViewById(R.id.PPE3));
        buttons.add((Button) findViewById(R.id.PPE4));
        buttons.add((Button) findViewById(R.id.PPE5));
        buttons.add((Button) findViewById(R.id.PPE6));
        buttons.add((Button) findViewById(R.id.PPE7));
        buttons.add((Button) findViewById(R.id.PPE8));
        buttons.add((Button) findViewById(R.id.PPE9));
        buttons.add((Button) findViewById(R.id.PPE10));

        texts.add((TextView) findViewById(R.id.PPEText1));
        texts.add((TextView) findViewById(R.id.PPEText2));
        texts.add((TextView) findViewById(R.id.PPEText3));
        texts.add((TextView) findViewById(R.id.PPEText4));
        texts.add((TextView) findViewById(R.id.PPEText5));
        texts.add((TextView) findViewById(R.id.PPEText6));
        texts.add((TextView) findViewById(R.id.PPEText7));
        texts.add((TextView) findViewById(R.id.PPEText8));
        texts.add((TextView) findViewById(R.id.PPEText9));
        texts.add((TextView) findViewById(R.id.PPEText10));

        int i = 0;
        for (DatabaseConnector.PPE ppe : DatabaseConnector.PPEList) {
            PPEButtons.put(ppe, buttons.get(i));
            PPETexts.put(ppe, texts.get(i));
            i++;
        }
    }

}







