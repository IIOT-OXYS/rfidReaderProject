package com.example.nzar.toyotarfid;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.felhr.usbserial.UsbSerialDebugger;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class SignOutActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "SignOutActivity";     //set debugging tag
    private UsbSerialDevice rfidReader;    //initialize serial device so all methods can access it


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_out);
        Button Contact = (Button) findViewById(R.id.Contact);   //initialize the contact button
        Contact.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*
         this block sets up all of the prerequisites for the USB Serial device library,
         then sets the serial device using the configuration used by the main activity.
         */
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        UsbDevice device = deviceList.get(MainActivity.UsbDeviceName);
        UsbDeviceConnection connection = manager.openDevice(device);
        rfidReader = UsbSerialDevice.createUsbSerialDevice(device, connection);


        if (rfidReader != null) {
            if (rfidReader.open()) {
                rfidReader.setBaudRate(9600);
                rfidReader.setDataBits(UsbSerialInterface.DATA_BITS_8);
                rfidReader.setStopBits(UsbSerialInterface.STOP_BITS_1);
                rfidReader.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                rfidReader.setParity(UsbSerialInterface.PARITY_ODD);
                rfidReader.setDTR(false);
                rfidReader.setRTS(false);
                rfidReader.read(mCallback);
            } else {
                Log.d(TAG, "could not open rfid reader");
            }
        } else {
            Log.d(TAG, "driver incorrect for rfid reader");
        }


    }

    /*
        This callback uses the currentEmployee set in the DatabaseConnector
     */
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

                    startActivity(new Intent(SignOutActivity.this, MainActivity.class));
                } else {
                    Log.d(TAG, "something went wrong signing out");
                }
            }
        }



    };

    @Override
    public void onClick(View v) {
        rfidReader.close();
        Intent contact = new Intent(this, TechContact.class);
        contact.putExtra("return", "SignOutActivity");
        this.startActivity(contact);
    }

}
