//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

/*
SettingsActivity:
This class is intended to allow system administrators to set many key parameters to work with their systems.
 */
public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    static SharedPreferences settings;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setContentView(R.layout.activity_settings);
        settings = getSharedPreferences("ConnectivitySettings",0);

            settings.registerOnSharedPreferenceChangeListener(this);
            Button setButton = (Button) findViewById(R.id.set_button);
            setButton.setOnClickListener(this);


        //setup UI elements for interaction
        View Focus = getCurrentFocus();

        Button BackButton = (Button) findViewById(R.id.SettingsCancelButton);
        Button UpdateButton = (Button) findViewById(R.id.SettingsLaunchUpdateButton);
        Button ResetButton = (Button) findViewById(R.id.SettingsResetButton);
        UpdateButton.setOnClickListener(this);
        ResetButton.setOnClickListener(this);
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
            case R.id.set_button:
                applySettings();
                Toast.makeText(this, "Parameters set", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
            case R.id.SettingsCancelButton:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.SettingsResetButton:
                clearSettings();
                Toast.makeText(this, "Settings cleared, please input new preferences before continuing", Toast.LENGTH_LONG).show();
                break;


        }
    }

    private void applySettings() {
        HashMap<String, EditText> SettingsFields = new HashMap<>();
        ConstraintLayout rootLayout = (ConstraintLayout) findViewById(R.id.activity_settings_root_layout);
        SharedPreferences.Editor editor = settings.edit();
        for (int i = 0; i < rootLayout.getChildCount(); i++) {
            View child = rootLayout.getChildAt(i);
            if (child instanceof EditText) {
                EditText field = (EditText) child;
                SettingsFields.put(field.toString().substring(field.toString().indexOf("id/") + 3, field.toString().length() - 1).trim(), field);
            }
        }
        for (String key : SettingsFields.keySet()) {
            EditText value = SettingsFields.get(key);
            if (!(value.getText().toString().trim().length() == 0)) {
                editor.putString(key, value.getText().toString());
            }
        }
        editor.apply();

    }

    private void clearSettings() {
        SharedPreferences.Editor editor = settings.edit();
        editor.clear().apply();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //run DB query to set equiment specs
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("hasEquipmentData", false);
        editor.putBoolean("hasNetworkConfig", false);
        editor.apply();
    }
}
