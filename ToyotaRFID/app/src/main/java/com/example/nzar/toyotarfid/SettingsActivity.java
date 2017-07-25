package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Button setButton = (Button) findViewById(R.id.set_button);
        Button BackButton = (Button) findViewById(R.id.back_button);

        setButton.setOnClickListener(this);
        BackButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.set_button:
                EditText dbUrl = (EditText) findViewById(R.id.db_url);
                EditText dbUser = (EditText) findViewById(R.id.db_user);
                EditText dbPasswd = (EditText) findViewById(R.id.db_pw);
                DatabaseConnector.setDatabaseRoot(dbUrl.getText().toString());
                DatabaseConnector.setDatabaseUser(dbUser.getText().toString());
                DatabaseConnector.setDatabasePasswd(dbPasswd.getText().toString());
                //DatabaseConnector.setStaticIP();
                break;
        }
    }
}
