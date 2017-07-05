package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class TechContact extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "TechContact";


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

        if (context != null) {
            switch (getIntent().getStringExtra("return")) {
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
                default:
                    startActivity(new Intent(this, MainActivity.class));
                    break;
            }
        } else {
            Log.d(TAG, "null intent extra");
            startActivity(new Intent(this, MainActivity.class));

        }



    }

}
