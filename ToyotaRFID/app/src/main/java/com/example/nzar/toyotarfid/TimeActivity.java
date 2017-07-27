package com.example.nzar.toyotarfid;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.felhr.usbserial.UsbSerialDevice;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class TimeActivity extends AppCompatActivity implements View.OnClickListener {

    final private String TAG = "TimeActivity";
    private final String ACTION_USB_PERMISSION = "com.android.example.nzar.toyotarfid.USB_PERMISSION";

    private Chronometer chron;
    private UsbSerialDevice relayController;
    private StringBuilder ID = new StringBuilder();
    private static boolean Finished;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        chron = (Chronometer) findViewById(R.id.chronometer2);
        final ToggleButton fin = (ToggleButton) findViewById(R.id.fin);
        final Button Contact = (Button) findViewById(R.id.Contact);
        chron.start();
        Contact.setOnClickListener(this);

        ID.delete(0, ID.length());

        try {
            relayController = attachUsbSerial(MainActivity.relayDeviceName, deviceList, manager);
            relayController.write("on".getBytes("ascii"));
        } catch (UnsupportedEncodingException | NullPointerException se) {
            se.printStackTrace();
        }

        fin.setBackgroundColor(Color.CYAN);

        Finished = false;

        fin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fin.setBackgroundColor(Color.GREEN);
                } else {
                    fin.setBackgroundColor(Color.CYAN);
                    ID.delete(0,ID.length());
                }
                Finished = isChecked;

            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Finished) {
            if (keyCode == KeyEvent.KEYCODE_BACKSLASH) {

                if (ID.toString().equals(MainActivity.ID.toString())) {
                    startActivity(new Intent(this, MainActivity.class));
                }

            } else {
                char c = (char) event.getUnicodeChar();
                ID.append(c);
            }
        }
        return super.onKeyDown(keyCode, event);

    }

    private UsbSerialDevice attachUsbSerial(String deviceName, HashMap<String, UsbDevice> deviceList, UsbManager manager) {
        if (deviceName != null) {

            UsbDevice device = deviceList.get(deviceName);
            UsbDeviceConnection connection = manager.openDevice(device);
            UsbSerialDevice serialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection);


            if (serialDevice != null) {
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                manager.requestPermission(device, mPermissionIntent);

                try {
                    serialDevice.open();

                    return serialDevice;
                } catch (NullPointerException se) {
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.Contact:
                Intent contact = new Intent(TimeActivity.this, TechContact.class);
                contact.putExtra("return", "TimeActivity");
                TimeActivity.this.startActivity(contact);//go to tech screen
                break;

        }

    }
}
