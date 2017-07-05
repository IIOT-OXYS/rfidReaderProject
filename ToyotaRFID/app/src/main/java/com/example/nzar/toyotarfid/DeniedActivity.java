package com.example.nzar.toyotarfid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DeniedActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denied);
        Button ret = (Button) findViewById(R.id.Return);
        ret.setOnClickListener(this);
        Button Contact = (Button) findViewById(R.id.Contact);
        Contact.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.Return :
                Intent main = new Intent(DeniedActivity.this, MainActivity.class);
                DeniedActivity.this.startActivity(main);
                break;
            case R.id.Contact :
                Intent contact = new Intent(DeniedActivity.this, TechContact.class);
                contact.putExtra("return", "DeniedActivity");
                DeniedActivity.this.startActivity(contact);
                break;
        }
        }


}


