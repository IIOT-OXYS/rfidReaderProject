//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

/*
This activity is where the user is placed if their ID does not match the training required for the device.
If the users ID is not found in the database, they will also land there.
 */

public class DeniedActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denied);
        //set up button click listeners
        Button ret = (Button) findViewById(R.id.Return);
        ret.setOnClickListener(this);
        Button Contact = (Button) findViewById(R.id.Contact);
        Contact.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //switch to navigate based on button pressed
        switch (v.getId()) {
            case R.id.Return:
                Intent main = new Intent(DeniedActivity.this, MainActivity.class);
                DeniedActivity.this.startActivity(main);
                break;
            case R.id.Contact:
                Intent contact = new Intent(DeniedActivity.this, TechContact.class);
                contact.putExtra("return", "DeniedActivity");
                DeniedActivity.this.startActivity(contact);
                break;
        }
    }



}



