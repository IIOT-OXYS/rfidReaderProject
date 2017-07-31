package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/*
SettingsActivity:
This class is intended to allow system administrators to set many key parameters to work with their systems.
 */
public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    static SharedPreferences settings;

    public static void setSettings(SharedPreferences settings) {
        SettingsActivity.settings = settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings.registerOnSharedPreferenceChangeListener(this);

        //setup UI elements for interaction
        View Focus = getCurrentFocus();
        Button setButton = (Button) findViewById(R.id.set_button);
        Button BackButton = (Button) findViewById(R.id.back_button);

        setButton.setOnClickListener(this);
        BackButton.setOnClickListener(this);


        if (Focus != null) {
            Focus.clearFocus();
        }

    }

    /*
    onClick:
    simple interupt method that detects UI interaction.
    This is used to navigate between activities using on-screen buttons.
     */
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
                EditText dbPort = (EditText) findViewById(R.id.db_port);
                EditText dbName = (EditText) findViewById(R.id.db_name);
                EditText dbEngine = (EditText) findViewById(R.id.db_engine);
                EditText StaticIP = (EditText) findViewById(R.id.static_ip);
                EditText SubnetMask = (EditText) findViewById(R.id.netmask);
                EditText WirelessSSID = (EditText) findViewById(R.id.wifi_ssid);
                EditText WirelessPasswd = (EditText) findViewById(R.id.wifi_pw);
                SharedPreferences.Editor settingsEditor = settings.edit();

                if (dbUrl.toString().length() > 2)
                    settingsEditor.putString("dbUrl", dbUrl.toString());
                if (dbPasswd.toString().length() > 2)
                    settingsEditor.putString("dbPasswd", dbPasswd.toString());
                if (dbUser.toString().length() > 2)
                    settingsEditor.putString("dbUser", dbUser.toString());
                if (dbPort.toString().length() > 1)
                    settingsEditor.putString("dbPort", dbPort.toString());
                if (dbName.toString().length() > 2)
                    settingsEditor.putString("dbName", dbName.toString());
                if (dbEngine.toString().length() > 2)
                    settingsEditor.putString("dbEngine", dbEngine.toString());
                if (StaticIP.toString().length() > 2)
                    settingsEditor.putString("StaticIP", StaticIP.toString());
                if (SubnetMask.toString().length() > 2)
                    settingsEditor.putString("SubnetMask", SubnetMask.toString());
                if (WirelessSSID.toString().length() > 2)
                    settingsEditor.putString("WirelessSSID", WirelessSSID.toString());
                if (WirelessPasswd.toString().length() > 2)
                    settingsEditor.putString("WirelessPasswd", WirelessPasswd.toString());
                settingsEditor.apply();



                Toast.makeText(this, "Parameters set", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                break;

        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //run DB query to set equiment specs
    }
}
