//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

/*
MainActivity:
This is the primary class of the application.
This class will initialize the RFID reader and poll it for ID numbers,
which are then parsed and fed into a database query to determine if the user who's ID was tapped
is allowed to use the attached device.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, DatabaseConnector.TILTPostUserTask.OnFinishedParsingListener {

    private final String TAG = "MainActivity";
    SharedPreferences settings;
    private final String ACTION_USB_PERMISSION = "com.android.example.nzar.toyotarfid.USB_PERMISSION";
    private final String RELAY_ON = "relay on 0\r";
    private final String RELAY_OFF = "relay off 0\r";
    /*
    ID: a string builder that gets the keystrokes from the RFID reader to be parsed and queried
    TAG: the debug tag used in Log statements
     */
    private StringBuilder ID = new StringBuilder();

    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setContentView(R.layout.activity_main);

        //get preferences and set network settings accordingly
        settings = getSharedPreferences("ConnectivitySettings", 0);

        if (DatabaseConnector.BindPreferences(settings)) {
            Toast.makeText(this, "WARNING: \n there are blank connection properties! \n The application will not work without these fields filled!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SettingsActivity.class));
        } else {
            Log.d(TAG, "Using serverIP: " + settings.getString("baseServerUrl", "null"));
            Log.d(TAG, "Using machineID: " + settings.getString("machineID", "null"));
        }

        //set up buttons with click listeners
        final Button Contact = (Button) findViewById(R.id.Contact);
        Contact.setOnClickListener(this);

        //show date
        TextView dateText = (TextView) findViewById(R.id.TextDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        dateText.setText(dateFormat.format(Calendar.getInstance().getTime()));


        //resets the string builder used to parse the input from the RFID reader.
        if (ID != null) {
            ID.delete(0, ID.length());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        setupRelay();

        TimerTask screenSaver = new TimerTask() {
            @Override
            public void run() {

            }
        };

        Timer screenSaverTimer = new Timer();

        screenSaverTimer.schedule(screenSaver, 300000);
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
        if (ID.length() == 1) {//inform the user their tap was registered

        }
        if (keyCode == KeyEvent.KEYCODE_BACKSLASH || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEMICOLON) {//checks for ascii delimiter
            final String badgeNumber = ID.toString().trim(); // builds the string from the string builder
            Log.d(TAG, "BadgeID: " + badgeNumber);//log ID for debugging
            DatabaseConnector.TILTPostUserTask Job = new DatabaseConnector.TILTPostUserTask();
            Job.setOnFinishedParsingListener(this);
            Job.setLoggingOut(false);
            Job.setSessionID(new Random().nextInt());
            Job.execute(badgeNumber);//execute the query on a separate thread
            TextView tv = (TextView) findViewById(R.id.main_activity_text);
            tv.setText("Checking certifications. . .");
            findViewById(R.id.MainActivityParent).setBackgroundColor(0xFF207ABE);
            Button Contact = (Button) findViewById(R.id.Contact);
            Contact.setBackgroundColor(0x88E55125);
            Contact.setEnabled(false);

        } else {//delimeter not detected, log input and proceed
            char c = (char) event.getUnicodeChar();
            ID.append(c);
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
    onTouchEvent:
    A special method that allows detection of more complex touch inputs.
    This is used to detect a secret four-finger tap gesture to navigate to the settings.
    This is done to allow administrators access to robust settings, while restricting access to users.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        long downTime = event.getDownTime();

        if (pointerCount == 3
                && SystemClock.uptimeMillis() - downTime >= 3000) {

//            MotionEvent.PointerCoords pointer0 = new MotionEvent.PointerCoords();
//            MotionEvent.PointerCoords pointer1 = new MotionEvent.PointerCoords();
//            MotionEvent.PointerCoords pointer2 = new MotionEvent.PointerCoords();
//            event.getPointerCoords(0, pointer0);
//            event.getPointerCoords(1, pointer1);
//            event.getPointerCoords(2, pointer2);

            //TODO add check for coordinates to be in correct area

            this.startActivity(new Intent(this, SettingsActivity.class));
            Log.d(TAG, "User has entered the admin settings");
            return true;

        } else {
            return super.onTouchEvent(event);
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
            case R.id.Contact:

                Intent contact = new Intent(this, TechContact.class);
                contact.putExtra("return", "MainActivity");
                this.startActivity(contact);
                break;
        }
    }


    @Override
    public void onFinishedParsing(DatabaseConnector.TILTPostUserTask Job) {
        try {
        String Authorization = Job.get();
        switch (Authorization) {
            case "UserIsTech":
            case "UserIsAllowed":
                startActivity(new Intent(this, CheckActivity.class));
                break;
            case "RequiresTech":
                startActivity(new Intent(this, SecondaryBadgeActivity.class));
                break;
            default:
                startActivity(new Intent(this, DeniedActivity.class));
                break;
        }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            ID.delete(0, ID.length());
            Toast.makeText(this, "Couldn't contact API server for certifications", Toast.LENGTH_LONG).show();
        }
    }
    private synchronized void setupRelay() {
        UsbSerialDevice relayDevice;
        //these objects are used to iterate through the active USB devices
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        //this iterates the hashmap and looks for a supported USB device
        for (UsbDevice device : deviceList.values()) {
            //if a compatible device is found, we ask for permission and attempt to close the relay
            if ((device.getProductId() == 0x0C05 && device.getVendorId() == 0x2A19) || device.getProductId() == 1155) {
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                manager.requestPermission(device, mPermissionIntent);
                relayDevice = attachUsbSerial(device.getDeviceName(), deviceList, manager);
                try {
                    assert relayDevice != null;
                    relayDevice.write(RELAY_OFF.getBytes("ASCII"));
                    Log.d(TAG, RELAY_OFF);
                } catch (Exception e) {
                    Log.d(TAG, "problem with relay controller, printing stack trace:");
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
    private synchronized UsbSerialDevice attachUsbSerial(String deviceName, HashMap<String, UsbDevice> deviceList, UsbManager manager) {
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



