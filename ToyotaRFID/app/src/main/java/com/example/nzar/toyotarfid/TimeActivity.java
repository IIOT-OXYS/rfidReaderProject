//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.felhr.usbserial.UsbSerialDevice;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

/*
TimeActivity:
This class is where the user goes when all checks have passed, and the user is actively using the equipment.
In this activity, the relay is closed, and the elapsed time is shown on the UI.
When the user is finished with the equipement, they hit the finish button, and are prompted to re-scan
their badge to prevent accidental logout
 */
public class TimeActivity extends AppCompatActivity implements View.OnClickListener, DatabaseConnector.TILTPostUserTask.OnFinishedParsingListener {

    private static boolean Finished;
    final private String TAG = "TimeActivity";
    private final String ACTION_USB_PERMISSION = "com.android.example.nzar.toyotarfid.USB_PERMISSION";
    private final String RELAY_ON = "relay on 0\r";
    private final String RELAY_OFF = "relay off 0\r";
    private Chronometer chron;
    long startTime = -1;
    private StringBuilder ID = new StringBuilder();
    private UsbSerialDevice relayDevice;
    private int TIMEOUT = 3600000;
    private Timer timer;

    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setContentView(R.layout.activity_time);

        //show date
        TextView dateText = (TextView) findViewById(R.id.TextDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        dateText.setText(dateFormat.format(Calendar.getInstance().getTime()));

        //ready the USB relay
        setupRelay();

        //set up our UI elements
        chron = (Chronometer) findViewById(R.id.chronometer2);
        final ToggleButton fin = (ToggleButton) findViewById(R.id.fin);
        final Button Contact = (Button) findViewById(R.id.Contact);
        long intentTime = getIntent().getLongExtra("timeTracker", -1);
        if (intentTime == -1) {
            startTime = SystemClock.elapsedRealtime();
        } else {
            startTime = intentTime;
        }
        chron.setBase(startTime);
        chron.start();

        Contact.setOnClickListener(this);

        //reset the string builder
        ID.delete(0, ID.length());

        //set disabled color to togglebutton
        Finished = false;
        fin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {//user wants to log out
                    ID.delete(0, ID.length());
                }
                //track the state of the toggle button for other methods
                Finished = isChecked;

            }
        });

    }

    private void startTimer(){
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
            refresh();
            }
        };
        timer.schedule(timerTask, TIMEOUT);
    }

    private void refresh() {
        Intent self = new Intent(this, getClass());
        self.putExtra("timeTracker", startTime);
        startActivity(self);
    }

    @Override
    protected void onStart() {
        super.onStart();
        relayOn();
        startTimer();
    }

    /*
        onKeyDown:
        This method grabs any keypresses to the system and runs this code when the key is pressed.
        This is used to get the badge scan from the RFID reader without a UI object to collect it.
        Once a specific delimiter character is detected, the method launches the query which checks
        if the user has the clearances to proceed, then launches either the check or denied activities.
         */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Finished) {
            if (keyCode == KeyEvent.KEYCODE_BACKSLASH) {
                final String badgeNumber = ID.toString().trim();
                Log.d(TAG, "BadgeID: " + badgeNumber);

                DatabaseConnector.TILTPostUserTask logoutTask = new DatabaseConnector.TILTPostUserTask();
                logoutTask.setOnFinishedParsingListener(this);
                logoutTask.setLoggingOut(true);
                logoutTask.setSessionID(DatabaseConnector.currentSessionID);
                logoutTask.execute(badgeNumber);


            } else {
                char c = (char) event.getUnicodeChar();
                ID.append(c);
            }


        }
        return super.onKeyDown(keyCode, event);

    }

    /*
  onClick:
  simple interrupt method that detects UI interaction.
  This is used to navigate between activities using on-screen buttons.
  */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.Contact:
                Intent contact = new Intent(TimeActivity.this, TechContact.class);
                contact.putExtra("return", "TimeActivity");
                contact.putExtra("timeTracker", startTime);
                TimeActivity.this.startActivity(contact);//go to tech screen
                break;

        }

    }

    private synchronized void setupRelay() {
        //these objects are used to iterate through the active USB devices
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        //this iterates the hashmap and looks for a supported USB device
        for (UsbDevice device : deviceList.values()) {
            Log.d(TAG, "Found USB device");
            Log.d(TAG, "VID: " + String.valueOf(device.getVendorId()));
            Log.d(TAG, "PID: " + String.valueOf(device.getProductId()));
            //if a compatible device is found, we ask for permission and attempt to close the relay
            if (device.getProductId() == 1155) {
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                manager.requestPermission(device, mPermissionIntent);
                UsbDeviceConnection connection = manager.openDevice(device);
                try {
                    relayDevice = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    relayDevice.setBaudRate(2400);
                    relayDevice.open();
                } catch (Exception e) {
                    refresh();
                }

                return;
            }
        }
    }

    private synchronized void relayOn(){
        try {
            assert relayDevice != null;
            relayDevice.write(RELAY_ON.getBytes("ASCII"));
            Log.d(TAG, RELAY_ON);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void relayOff(){
        try {
            assert relayDevice != null;
            relayDevice.write(RELAY_OFF.getBytes("ASCII"));
            Log.d(TAG, RELAY_OFF);

        } catch (Exception e) {
            Toast.makeText(this, "Relay controller failed, contact administrator.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    @Override
    public void onFinishedParsing(DatabaseConnector.TILTPostUserTask logoutTask) {
        try {
            if (logoutTask.get().equals("Logout")) {

                relayOff();
                startActivity(new Intent(this, MainActivity.class));

            } else {
                ID.delete(0, ID.length());
                Toast.makeText(this, "Only the user which accessed the machine or a tech may badge out.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
        timer.purge();
    }
}


