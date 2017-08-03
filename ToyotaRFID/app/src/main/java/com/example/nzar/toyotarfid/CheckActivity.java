package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class CheckActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "CzechActivity";
    private static int PPECount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this gets rid of title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_check);

        //creating all the buttons and toggle button
        final Button yes = (Button) findViewById(R.id.Yes);
        yes.setOnClickListener(this);
        yes.setEnabled(false);
        final Button cancel = (Button) findViewById(R.id.cancel_action);
        cancel.setOnClickListener(this);
        final Button contact = (Button) findViewById(R.id.Contact);
        contact.setOnClickListener(this);
        AsyncTask<Void, Void, String> setPPE = new CheckActivity.PPEJob();
        setPPE.execute();
        try{
            String PPE = setPPE.get();
            TextView requirements = (TextView) findViewById(R.id.PPERequirements);
            requirements.setText(PPE);
        }
        catch (InterruptedException | ExecutionException | NullPointerException e){
            e.printStackTrace();
        }


        HashMap<String, ImageButton> PPEButtons = generatePPEButtons();
        for (ImageButton PPEButton : PPEButtons.values()) {
            PPEButton.setVisibility(View.VISIBLE);
            PPEButton.setOnClickListener(this);
            PPECount++;
        }
        if (PPECount > 10) PPECount--; // bad hack because two buttons overlap

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

    private HashMap<String, ImageButton> generatePPEButtons() {
        HashMap<String, ImageButton> PPEButtons = new HashMap<>();
        ConstraintLayout rootLayout = (ConstraintLayout) findViewById(R.id.check_activity_root_layout);
        for (int i = 0; i < rootLayout.getChildCount(); i++) {
            View child = rootLayout.getChildAt(i);
            if (child instanceof ImageButton) {
                ImageButton PPEButton = (ImageButton) child;
                PPEButtons.put(PPEButton.toString().substring(PPEButton.toString().indexOf("id/") + 3, PPEButton.toString().length() - 1).trim(), PPEButton);
            }
        }
        return PPEButtons;
    }

    private class PPEJob extends AsyncTask< Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                return DatabaseConnector.getPPE();
            } catch (SQLException | ClassNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}







