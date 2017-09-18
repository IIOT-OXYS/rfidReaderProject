package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

public class SecondaryBadgeActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary_badge);


        Button Cancel = (Button) findViewById(R.id.SecondaryBadgeInCancel);
        Button ContactTech = (Button) findViewById(R.id.SecondaryBadgeInContactTech);

        Cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.SecondaryBadgeInCancel:
                new DatabaseConnector.TILTPostUserTask()
                        .execute(DatabaseConnector.currentBadgeID, String.valueOf(DatabaseConnector.currentSessionID));
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.SecondaryBadgeInContactTech:
                Intent contactTech = new Intent(this, CheckActivity.class);
                contactTech.putExtra("return", "SecondaryBadgeIn");
                startActivity(contactTech);
        }
    }
}

