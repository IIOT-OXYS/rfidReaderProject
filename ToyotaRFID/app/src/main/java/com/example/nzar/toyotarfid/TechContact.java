package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
        Button back = (Button) findViewById(R.id.Back);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

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
                    startActivity(new Intent(this, TimeActivity.class));
                    break;
                case "CheckActivity":
                    startActivity(new Intent(this, CheckActivity.class));
                    break;
                case "DeniedActivity":
                    startActivity(new Intent(this, DeniedActivity.class));
                    break;
                default:
                    startActivity(new Intent(this, MainActivity.class));
                    Toast.makeText(this, "Something happened, contact OXYS and tell them \"contact tech got confused\".", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            Log.d(TAG, "null intent extra"); // hopefully this doesn't happen, but if it does, we're ready
            startActivity(new Intent(this, MainActivity.class));
            Toast.makeText(this, "Something really bad happened, contact OXYS and tell them \"contact tech went rogue\"", Toast.LENGTH_SHORT).show();

        }



    }

}
