//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

/*
TechContact:
This class provides contact information about how to contact the working technician.
The user also has the ability to ping a tech using email (no current implementation)
 */
public class TechContact extends AppCompatActivity implements View.OnClickListener, DatabaseConnector.TILTGetTechTask.OnFinishedParsingListener, DatabaseConnector.TILTPostTechTask.OnSentEmailListener {

    private final String TAG = "TechContact";   // set Log tag

    private final int TIMEOUT = 300000;
    private Timer timer;

    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_tech_contact);
        //setup UI elements for interaction
        Button back = (Button) findViewById(R.id.tech_back_button);
        back.setOnClickListener(this);

        //show date
        TextView dateText = (TextView) findViewById(R.id.TextDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        dateText.setText(dateFormat.format(Calendar.getInstance().getTime()));


        DatabaseConnector.TILTGetTechTask refreshTechs = new DatabaseConnector.TILTGetTechTask();
        refreshTechs.setOnFinishedParsingListener(this);
        refreshTechs.execute();

        startTimer();

    }

    private void startTimer(){
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                contextSwitch(getIntent().getStringExtra("return"));
            }
        };
        timer.schedule(timerTask, TIMEOUT);
    }

    @Override
    public synchronized void onClick(View v) {

        switch (v.getId()) {


            case R.id.tech_page_button:
                v.setBackgroundColor(0x88E55125);
                findViewById(R.id.pagingTechText).setVisibility(View.VISIBLE);
                findViewById(R.id.ContactTechParent).setBackgroundColor(0xFF207ABE);
                findViewById(R.id.constraintLayout).setVisibility(View.INVISIBLE);
                findViewById(R.id.constraintLayout2).setVisibility(View.INVISIBLE);

                try {
                    DatabaseConnector.TILTPostTechTask TechEmail = new DatabaseConnector.TILTPostTechTask();
                    TechEmail.setOnSentEmailListener(this);
                    if (getIntent().getStringExtra("return").equals("MainActivity")) {
                        TechEmail.execute();
                    } else {
                        TechEmail.execute(String.valueOf(DatabaseConnector.currentSessionID));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "There was a problem attempting to contact the technicians", Toast.LENGTH_SHORT).show();
                    v.setBackgroundColor(0xFFE55125);

                }
                break;

            case R.id.tech_back_button:
        /*
            In this particular onClickListener, we grab a StringExtra that every activity sets
            before sending the intent to get to this activity. We use that extra as a context
            to go back exactly to the activity that created the Intent.
         */
            contextSwitch(getIntent().getStringExtra("return"));

                break;

        }


    }

    private void contextSwitch(String context) {
        timer.cancel();
        timer.purge();
        if (context != null) {
            Log.d(TAG, context);
            switch (context) {
                case "MainActivity":
                    startActivity(new Intent(this, MainActivity.class));
                    break;
                case "TimeActivity":
                    Intent timeIntent = new Intent(this, TimeActivity.class);
                    timeIntent.putExtra("timeTracker", getIntent().getLongExtra("timeTracker", -1));
                    startActivity(timeIntent);
                    break;
                case "CheckActivity":
                    startActivity(new Intent(this, CheckActivity.class));
                    break;
                case "DeniedActivity":
                    startActivity(new Intent(this, DeniedActivity.class));
                    break;
                case "SecondaryBadgeIn":
                    startActivity(new Intent(this, SecondaryBadgeActivity.class));
                    break;
                default:
                    startActivity(new Intent(this, MainActivity.class));
                    Toast.makeText(this, "Something happened, contact OXYS and tell them \"contact tech got confused\".", Toast.LENGTH_LONG).show();
                    break;
            }
        } else {
            Log.d(TAG, "null intent extra"); // hopefully this doesn't happen, but if it does, we're ready
            startActivity(new Intent(this, MainActivity.class));
            Toast.makeText(this, "Something really bad happened, contact OXYS and tell them \"contact tech went rogue\"", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onFinishedParsing() {
        ConstraintLayout techContainer = (ConstraintLayout) findViewById(R.id.constraintLayout);
        ConstraintLayout tech2Container = (ConstraintLayout) findViewById(R.id.constraintLayout2);
        Button ping = (Button) findViewById(R.id.tech_page_button);
        if (DatabaseConnector.LabTechList.isEmpty()) {
            ping.setBackgroundColor(0x88E55125);
            techContainer.setVisibility(View.INVISIBLE);
            tech2Container.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "No lab techs currently active.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "no lab techs to display");
            return;
        }
        switch (DatabaseConnector.LabTechList.size()){
            default:

            case 2:
                tech2Container.setVisibility(View.VISIBLE);
                DatabaseConnector.LabTech tech2 = DatabaseConnector.LabTechList.get(1);
                TextView name2 = (TextView) findViewById(R.id.LabTechName2);
                TextView email2 = (TextView) findViewById(R.id.LabTechEmail2);
                TextView phone2 = (TextView) findViewById(R.id.LabTechPhoneNumber2);
                ImageView image2 = (ImageView) findViewById(R.id.LabTechImage2);

                if (tech2.firstName != null && tech2.lastName != null) {
                    name2.setText(tech2.firstName + " " + tech2.lastName);
                }
                if (tech2.email != null) {
                    email2.setText(tech2.email);
                }
                if (tech2.phoneNumber != null) {
                    phone2.setText(tech2.phoneNumber);
                }
                if (tech2.Image != null) {
                    image2.setBackground(tech2.Image);
                }
            case 1:
                techContainer.setVisibility(View.VISIBLE);
                DatabaseConnector.LabTech tech = DatabaseConnector.LabTechList.get(0);
                TextView name = (TextView) findViewById(R.id.LabTechName);
                TextView email = (TextView) findViewById(R.id.LabTechEmail);
                TextView phone = (TextView) findViewById(R.id.LabTechPhoneNumber);
                ImageView image = (ImageView) findViewById(R.id.LabTechImage);

                if (tech.firstName != null && tech.lastName != null) {
                    name.setText(tech.firstName + " " + tech.lastName);
                }
                if (tech.email != null) {
                    email.setText(tech.email);
                }
                if (tech.phoneNumber != null) {
                    phone.setText(tech.phoneNumber);
                }
                if (tech.Image != null) {
                    image.setBackground(tech.Image);
                }

                ping.setOnClickListener(this);

                break;
            case 0:
                ping.setBackgroundColor(0x88E55125);
                techContainer.setVisibility(View.INVISIBLE);
                tech2Container.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "No lab techs currently active.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "no lab techs to display");
                break;



        }
    }


    @Override
    public void onSentEmail(DatabaseConnector.TILTPostTechTask TechEmail) {
        try {
            if (TechEmail.get().equals(false)) {
                Toast.makeText(this, "There was a problem attempting to contact the technicians", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        contextSwitch(getIntent().getStringExtra("return"));

    }

    @Override
    protected void onStop() {
        timer.cancel();
        timer.purge();
        super.onStop();
    }
}
