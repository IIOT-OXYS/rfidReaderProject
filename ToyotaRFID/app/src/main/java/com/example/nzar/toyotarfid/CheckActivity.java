//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;

public class CheckActivity extends AppCompatActivity implements View.OnClickListener {

    private static int PPECount = 0;
    private final String TAG = "CzechActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        //creating all the buttons and toggle button
        final Button yes = (Button) findViewById(R.id.Yes);
        yes.setOnClickListener(this);
        yes.setEnabled(false);
        final Button cancel = (Button) findViewById(R.id.cancel_action);
        cancel.setOnClickListener(this);
        final Button contact = (Button) findViewById(R.id.Contact);
        contact.setOnClickListener(this);


        HashMap<DatabaseConnector.PPE, Button> PPEButtons = generatePPEButtons();
        for (DatabaseConnector.PPE ppe : PPEButtons.keySet()) {
            //for final implementation, there will be logic to decide if the button should be enabled
            //based on the database query
            Button ppebutton = PPEButtons.get(ppe);
            ppebutton.setVisibility(View.VISIBLE);
            ppebutton.setBackground(ppe.Image);
            ppebutton.setOnClickListener(this);
        }
        if (PPEButtons.size() <= 0) yes.setEnabled(true);

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
            case R.id.cancel_action:
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
                    }
                }
                break;
        }
    }

    //method for generating the ImageButtons used to continue to the TimeActivity
    private HashMap<DatabaseConnector.PPE, Button> generatePPEButtons() {
        HashMap<DatabaseConnector.PPE, Button> PPEButtons = new HashMap<>();
        ArrayList<Button> buttons = new ArrayList<>();

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

        int i = 0;
        for (DatabaseConnector.PPE PPE : DatabaseConnector.PPEList) {
            if (PPE.Required) {
                PPEButtons.put(PPE, buttons.get(i));
                i++;
            }
        }
        return PPEButtons;
    }

}







