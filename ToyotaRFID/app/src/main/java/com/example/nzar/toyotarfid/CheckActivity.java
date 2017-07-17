package com.example.nzar.toyotarfid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import static android.R.string.yes;

public class CheckActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this gets rid of title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_check);

        //creating all the buttons and toggle button
        final Button yes = (Button) findViewById(R.id.Yes);
        yes.setOnClickListener(this);
        final Button cancel = (Button) findViewById(R.id.cancel_action);
        cancel.setOnClickListener(this);
        final Button contact = (Button) findViewById(R.id.Contact);
        contact.setOnClickListener(this);
        final ToggleButton toggleAgree = (ToggleButton) findViewById(R.id.toggleButton);

        //set the yes button to disabled so you have to agree
//        yes.setEnabled(false);
//        cancel.setEnabled(true);
//        contact.setEnabled(true);
        //disabled temporarily for incorporating imagebuttons


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
            //currently a place holder for bringing you back to badge reader
            case R.id.cancel_action:
                Intent x = new Intent(CheckActivity.this, MainActivity.class);
                startActivity(x);
                break;
            //will eventually contact a system tech
            case R.id.Contact:
                Intent contact = new Intent(CheckActivity.this, TechContact.class);
                contact.putExtra("return", "CheckActivity");
                startActivity(contact);
                break;
            default:
                break;
        }
    }
}






