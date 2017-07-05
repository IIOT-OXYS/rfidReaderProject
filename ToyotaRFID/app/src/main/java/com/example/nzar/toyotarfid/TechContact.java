package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class TechContact extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "TechContact";   // set Log tag


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tech_contact);
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
                case "SignOutActivity":
                    startActivity(new Intent(this, SignOutActivity.class));
                    break;
                default:
                    startActivity(new Intent(this, MainActivity.class));
                    break;
            }
        } else {
            Log.d(TAG, "null intent extra"); // hopefully this doesn't happen, but if it does, we're ready
            startActivity(new Intent(this, MainActivity.class));

        }



    }

}
