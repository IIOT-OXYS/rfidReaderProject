package com.example.nzar.toyotarfid;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static String relayDeviceName = null;
    public static StringBuilder ID = new StringBuilder();
    private final String ACTION_USB_PERMISSION = "com.android.example.nzar.toyotarfid.USB_PERMISSION";
    private final String TAG = "MainActivity";

    //private View mDecorView = getWindow().getDecorView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button Contact = (Button) findViewById(R.id.Contact);
        Contact.setOnClickListener(this);
        if (ID != null) {
            ID.delete(0, ID.length());
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACKSLASH) {
            String badgeNumber = ID.toString().trim();
            Log.d(TAG, badgeNumber);
            AsyncTask<String, Integer, Boolean> Job = new DatabaseJob();
            Job.execute(badgeNumber);

            try {
                Boolean Allowed = Job.get();
                if (Allowed) {
                    startActivity(new Intent(this, CheckActivity.class));
                } else {
                    startActivity(new Intent(this, DeniedActivity.class));
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }


        } else {
            char c = (char) event.getUnicodeChar();
            ID.append(c);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getPointerCount() == 4) {
            this.startActivity(new Intent(this, SettingsActivity.class));
            return super.onTouchEvent(event);

        } else {
            return super.onTouchEvent(event);
        }


    }

    /*
        Standard onClick to move between activities using the buttons.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Contact:

                Intent contact = new Intent(this, TechContact.class);
                contact.putExtra("return", "MainActivity");
                this.startActivity(contact);
                break;
        }
    }

    private class DatabaseJob extends AsyncTask<String, Integer, Boolean> {

        protected Boolean doInBackground(String... params) {
            boolean AccessGranted = false;

            int badgeNumber = Integer.parseInt(params[0]);
            try {
                AccessGranted = DatabaseConnector.EmployeeAuthorized(badgeNumber);
            } catch (SQLException | ClassNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return AccessGranted;

        }
    }
}



