//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

/*
MainActivity:
This is the primary class of the application.
This class will initialize the RFID reader and poll it for ID numbers,
which are then parsed and fed into a database query to determine if the user who's ID was tapped
is allowed to use the attached device.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "MainActivity";
    SharedPreferences settings;
    /*
    ID: a string builder that gets the keystrokes from the RFID reader to be parsed and queried
    TAG: the debug tag used in Log statements
     */
    private StringBuilder ID = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get preferences and set network settings accordingly
        settings = getPreferences(0);

        try {
             new DatabaseConnector.TILTGetTechTask().execute();
        } catch (Exception e) {
            e.printStackTrace();
            DatabaseConnector.LabTech errorTech = new DatabaseConnector.LabTech();
            errorTech.firstName = "Error";
            errorTech.LabTechID = 0;
            errorTech.lastName = "Error";
            errorTech.email = "Error";
            DatabaseConnector.LabTechList.add(errorTech);
            Toast.makeText(this, "There was a problem updating the Active Tech List", Toast.LENGTH_LONG).show();
        }

//        try {
//            DatabaseConnector.SetNetworkConfigTask setupNetwork = new DatabaseConnector.SetNetworkConfigTask();
//            setupNetwork.execute(getApplicationContext());
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "There was a problem configuring the network", Toast.LENGTH_SHORT).show();
//        }

        //set up buttons with click listeners
        final Button Contact = (Button) findViewById(R.id.Contact);
        Contact.setOnClickListener(this);

        //resets the string builder used to parse the input from the RFID reader.
        if (ID != null) {
            ID.delete(0, ID.length());
        }

    }

    /*
    onKeyDown:
    This method grabs any keypresses to the system and runs this code when the key is pressed.
    This is used to get the badge scan from the RFID reader without a UI object to collect it.
    Once a specific delimiter character is detected, the method launches the query which checks
    if the user has the clearances to proceed, then launches either the check or denied activities.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (ID.length() == 0) {//inform the user their tap was registered
            TextView tv = (TextView) findViewById(R.id.main_activity_text);
            tv.setText("Checking certifications. . .");
        }
        if (keyCode == KeyEvent.KEYCODE_BACKSLASH || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEMICOLON) {//checks for ascii delimiter
            Log.d(TAG, ID.toString().trim());//log ID for debugging
            String badgeNumber = ID.toString().trim(); // builds the string from the string builder
            DatabaseConnector.TILTPostUserTask Job = new DatabaseConnector.TILTPostUserTask();
            Job.execute(badgeNumber);//execute the query on a separate thread





            try {
                wait(10000);
                Boolean Allowed = Job.get();
                if (Allowed) {
                    startActivity(new Intent(this, CheckActivity.class));
                } else {
                    startActivity(new Intent(this, DeniedActivity.class));
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Toast.makeText(this, "Couldn't contact API server for certifications", Toast.LENGTH_LONG).show();
            }


        } else {//delimeter not detected, log input and proceed
            char c = (char) event.getUnicodeChar();
            ID.append(c);
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
    onTouchEvent:
    A special method that allows detection of more complex touch inputs.
    This is used to detect a secret four-finger tap gesture to navigate to the settings.
    This is done to allow administrators access to robust settings, while restricting access to users.
     */
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//        if (event.getPointerCount() == 4) {
//            SettingsActivity.setSettings(settings);
//            this.startActivity(new Intent(this, SettingsActivity.class));
//            return super.onTouchEvent(event);
//
//        } else {
//            return super.onTouchEvent(event);
//        }
//
//
//    }

    /*
    onClick:
    simple interupt method that detects UI interaction.
    This is used to navigate between activities using on-screen buttons.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Contact:

                Intent contact = new Intent(this, TechContact.class);
                contact.putExtra("return", "MainActivity");
                this.startActivity(contact);
                break;
        }
    }





}



