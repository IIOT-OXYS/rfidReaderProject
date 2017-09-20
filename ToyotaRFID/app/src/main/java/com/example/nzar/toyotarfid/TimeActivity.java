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
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.felhr.usbserial.UsbSerialDevice;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

/*
TimeActivity:
This class is where the user goes when all checks have passed, and the user is actively using the equipment.
In this activity, the relay is closed, and the elapsed time is shown on the UI.
When the user is finished with the equipement, they hit the finish button, and are prompted to re-scan
their badge to prevent accidental logout
 */
public class TimeActivity extends AppCompatActivity implements View.OnClickListener {

    private static boolean Finished;
    final private String TAG = "TimeActivity";
    private final String ACTION_USB_PERMISSION = "com.android.example.nzar.toyotarfid.USB_PERMISSION";
    private final String RELAY_ON = "relay on 0\r";
    private final String RELAY_OFF = "relay off 0\r";
    private Chronometer chron;
    long startTime = -1;
    private StringBuilder ID = new StringBuilder();
    private UsbSerialDevice relayDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setContentView(R.layout.activity_time);

        //ready the USB relay
        setupRelay();

        //set up our UI elements
        chron = ( Chronometer ) findViewById(R.id.chronometer2);
        final ToggleButton fin = ( ToggleButton ) findViewById(R.id.fin);
        final Button Contact = ( Button ) findViewById(R.id.Contact);
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
                logoutTask.execute(badgeNumber, String.valueOf(DatabaseConnector.currentSessionID));
                try {
                if (logoutTask.get().equals("UserIsTech") || logoutTask.get().equals("UserIsAllowed")) {

                    relayDevice.write(RELAY_OFF.getBytes("ASCII"));
                    Log.d(TAG, RELAY_OFF);
                    startActivity(new Intent(this, MainActivity.class));

                }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }



                } else {
                    ID.delete(0, ID.length());
                    Toast.makeText(this, "Only the user which accessed the machine or a tech may badge out.", Toast.LENGTH_SHORT).show();
                }

            } else {
                char c = ( char ) event.getUnicodeChar();
                ID.append(c);
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
        UsbManager manager = ( UsbManager ) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        //this iterates the hashmap and looks for a supported USB device
        for (UsbDevice device : deviceList.values()) {
            //if a compatible device is found, we ask for permission and attempt to close the relay
            if ((device.getProductId() == 0x0C05 && device.getVendorId() == 0x2A19) || device.getProductId() == 1155) {
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                manager.requestPermission(device, mPermissionIntent);
                relayDevice = attachUsbSerial(device.getDeviceName(), deviceList, manager);
                try {
                    relayDevice.write(RELAY_ON.getBytes("ASCII"));
                    Log.d(TAG, RELAY_ON);
                } catch (UnsupportedEncodingException | NullPointerException e) {
                    Toast.makeText(this, "Relay controller failed, contact administrator.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    /*
    attachUsbSerial:
    Uses a library to set up a serial terminal with the relay device
     */
    private UsbSerialDevice attachUsbSerial(String deviceName, HashMap<String, UsbDevice> deviceList, UsbManager manager) {
        if (deviceName != null) {

            UsbDevice device = deviceList.get(deviceName);
            UsbDeviceConnection connection = manager.openDevice(device);
            UsbSerialDevice serialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection);

            if (serialDevice != null) {

                try {
                    serialDevice.open();

                    return serialDevice;
                } catch (NullPointerException se) {
                    Log.d(TAG, "serial device connection lost");
                    se.printStackTrace();
                    return null;
                }
            } else {
                Log.d(TAG, "driver incorrect for rfid reader");
                return null;
            }

        } else {
            Log.d(TAG, "no viable target device was found");
            return null;
        }
    }

}


