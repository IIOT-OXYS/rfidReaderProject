package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    final static boolean ENABLED = true;
    final static boolean DISABLED = false;
    static boolean settingsEnabled = ENABLED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        View Focus = getCurrentFocus();

        Button setButton = (Button) findViewById(R.id.set_button);
        Button BackButton = (Button) findViewById(R.id.back_button);

        setButton.setOnClickListener(this);
        BackButton.setOnClickListener(this);


        if (Focus != null) {
            Focus.clearFocus();
        }
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
                Toast.makeText(this, "Parameters set", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                break;

        }
    }
}
