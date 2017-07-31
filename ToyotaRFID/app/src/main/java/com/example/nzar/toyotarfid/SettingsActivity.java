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

                if (notNull(dbUrl)) {
                    settingsEditor.putString("dbUrl", dbUrl.getText().toString());
                }
                if (notNull(dbPasswd)) {
                    settingsEditor.putString("dbPasswd", dbPasswd.getText().toString());
                }
                if (notNull(dbUser)) {
                    settingsEditor.putString("dbUser", dbUser.getText().toString());
                }
                if (notNull(dbPort)) {
                    settingsEditor.putString("dbPort", dbPort.getText().toString());
                }
                if (notNull(dbName)) {
                    settingsEditor.putString("dbName", dbName.getText().toString());
                }
                if (notNull(dbEngine)) {
                    settingsEditor.putString("dbEngine", dbEngine.getText().toString());
                }
                if (notNull(StaticIP)) {
                    settingsEditor.putString("StaticIP", StaticIP.getText().toString());
                }
                if (notNull(SubnetMask)) {
                    settingsEditor.putString("SubnetMask", SubnetMask.getText().toString());
                }
                if (notNull(WirelessSSID)) {
                    settingsEditor.putString("WirelessSSID", WirelessSSID.getText().toString());
                }
                if (notNull(WirelessPasswd)) {
                    settingsEditor.putString("WirelessPasswd", WirelessPasswd.getText().toString());
                }
                settingsEditor.apply();


                Toast.makeText(this, "Parameters set", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                break;

        }
    }

    private boolean notNull(EditText etText) {
        return !(etText.getText().toString().trim().length() == 0);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //run DB query to set equiment specs
    }
}
