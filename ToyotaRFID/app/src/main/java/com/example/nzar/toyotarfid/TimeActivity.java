package com.example.nzar.toyotarfid;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class TimeActivity extends AppCompatActivity implements View.OnClickListener {

    final private String TAG = "TimeActivity";
    private Chronometer chron;
    private UsbSerialDevice rfidReader;    //initialize serial device so all methods can access it



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);
         chron = (Chronometer) findViewById(R.id.chronometer2);
        final ToggleButton fin = (ToggleButton) findViewById(R.id.fin);
        final Button Contact = (Button) findViewById(R.id.Contact);
        chron.start();
        Contact.setOnClickListener(this);
        fin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                    HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                    UsbDevice device = deviceList.get(MainActivity.UsbDeviceName);
                    UsbDeviceConnection connection = manager.openDevice(device);
                    rfidReader = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (rfidReader.open()) {rfidReader.read(mCallback);}

                } else {

                    rfidReader.close();
                    //stop USB reader
                }
            }
        });

    }

    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] bytes) {
            if (bytes.length > 2) {
                String id = null;
                try {
                    id = new String(bytes, "ascii");
                } catch (UnsupportedEncodingException se) {
                    se.printStackTrace();
                }
                if (id != null && id.trim().equals(DatabaseConnector.getCurrentEmployee().ID)) {
                    //shut off relay
                    rfidReader.close();
                    chron.stop();

                    startActivity(new Intent(TimeActivity.this, MainActivity.class));
                } else {
                    Log.d(TAG, "something went wrong signing out");
                }
            }
        }



    };


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
