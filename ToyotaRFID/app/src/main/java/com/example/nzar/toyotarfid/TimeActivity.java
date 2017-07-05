package com.example.nzar.toyotarfid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

public class TimeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);
        final Chronometer chron = (Chronometer) findViewById(R.id.chronometer2);
        final Button fin = (Button) findViewById(R.id.fin);
        final Button Contact = (Button) findViewById(R.id.Contact);
        chron.start();
        fin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chron.stop();
                startActivity(new Intent(TimeActivity.this, SignOutActivity.class));
            }
        });
        Contact.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent contact = new Intent(TimeActivity.this, TechContact.class);
                contact.putExtra("return", "TimeActivity");
                TimeActivity.this.startActivity(contact);
            }
        });
    }
}
