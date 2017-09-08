//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/*
TechContact:
This class provides contact information about how to contact the working technician.
The user also has the ability to ping a tech using email (no current implementation)
 */
public class TechContact extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "TechContact";   // set Log tag


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tech_contact);
        //setup UI elements for interaction
        Button back = (Button) findViewById(R.id.tech_back_button);
        back.setOnClickListener(this);
        Button ping = (Button) findViewById(R.id.tech_page_button);
        ping.setOnClickListener(this);

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
        } finally {
            DatabaseConnector.LabTech tech = DatabaseConnector.LabTechList.get(0);
            TextView name = (TextView) findViewById(R.id.LabTechName);
            TextView email = (TextView) findViewById(R.id.LabTechEmail);
            TextView phone = (TextView) findViewById(R.id.LabTechPhoneNumber);
            ImageView image = (ImageView) findViewById(R.id.LabTechImage);

            name.setText(tech.firstName + " " + tech.lastName);
            email.setText(tech.email);
            phone.setText(tech.phoneNumber);
            image.setBackground(tech.Image);

        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.tech_back_button:
                String context = getIntent().getStringExtra("return");
        /*
            In this particular onClickListener, we grab a StringExtra that every activity sets
            before sending the intent to get to this activity. We use that extra as a context
            to go back exactly to the activity that created the Intent.
         */
                if (context != null) {
                    Log.d(TAG, context);
                    switch (context) {
                        case "MainActivity":
                            startActivity(new Intent(this, MainActivity.class));
                            break;
                        case "TimeActivity":
                            Intent timeIntent = new Intent(this, TimeActivity.class);
                            timeIntent.putExtra("timeTracker", getIntent().getLongExtra("timeTracker",-1));
                            startActivity(timeIntent);
                            break;
                        case "CheckActivity":
                            startActivity(new Intent(this, CheckActivity.class));
                            break;
                        case "DeniedActivity":
                            startActivity(new Intent(this, DeniedActivity.class));
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
                break;
            case R.id.tech_page_button:
                try{
                    DatabaseConnector.TILTPostTechTask TechEmail = new DatabaseConnector.TILTPostTechTask();
                    TechEmail.execute();
                    Toast.makeText(this, "Email sent successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "There was a problem attempting to contact the technicians", Toast.LENGTH_SHORT).show();
                }
                break;
        }


    }

}
