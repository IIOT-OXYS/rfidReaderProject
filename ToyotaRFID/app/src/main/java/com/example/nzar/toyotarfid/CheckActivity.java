package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

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
        ImageButton glovesOn = (ImageButton) findViewById(R.id.gloves_on_button);
        ImageButton earProtection = (ImageButton) findViewById(R.id.ear_protection_button);
        ImageButton noJewelery = (ImageButton) findViewById(R.id.no_jewelery_button);
        ImageButton faceShield = (ImageButton) findViewById(R.id.face_shield_button);
        ImageButton longHair = (ImageButton) findViewById(R.id.long_hair_button);
        ImageButton footWare = (ImageButton) findViewById(R.id.footware_button);
        ImageButton respiratoryProtection = (ImageButton) findViewById(R.id.respiratory_protection_button);
        ImageButton eyeProtection = (ImageButton) findViewById(R.id.eye_protection_button);
        ImageButton weldingMask = (ImageButton) findViewById(R.id.welding_mask_button);
        ImageButton protectiveClothing = (ImageButton) findViewById(R.id.protective_clothing_button);
        glovesOn.setVisibility(View.VISIBLE);
        earProtection.setVisibility(View.VISIBLE);
        noJewelery.setVisibility(View.VISIBLE);
        faceShield.setVisibility(View.VISIBLE);
        longHair.setVisibility(View.VISIBLE);
        footWare.setVisibility(View.VISIBLE);
        respiratoryProtection.setVisibility(View.VISIBLE);
        eyeProtection.setVisibility(View.VISIBLE);
        weldingMask.setVisibility(View.VISIBLE);
        protectiveClothing.setVisibility(View.VISIBLE);
        //set the yes button to disabled so you have to agree
//        yes.setEnabled(false);
//        cancel.setEnabled(true);
//        contact.setEnabled(true);
        //disabled temporarily for incorporating imagebuttons
        //TODO implement imagebuttons with query
        /*
        currently all imagebuttons are set to invisible.
        There will be a query that happens at startup that determines the PPE of the device.
        At that point those PPE buttons will be made visible, and have their onclicklisteners implemented.
        The Yes button will then be disabled until the appropriate buttons have been checked
         */


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
                Intent x = new Intent(CheckActivity.this, MainActivity.class);
                startActivity(x);
                break;
            //goes to the contact tech page
            case R.id.Contact:
                Intent contact = new Intent(CheckActivity.this, TechContact.class);
                contact.putExtra("return", "CheckActivity");
                startActivity(contact);
                break;
            default:
                break;
        }
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







