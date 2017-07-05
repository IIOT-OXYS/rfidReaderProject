package com.example.nzar.toyotarfid;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDebugger;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.sql.*;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String ACTION_USB_PERMISSION = "com.android.example.nzar.toyotarfid.USB_PERMISSION";
    private final String TAG = "MainActivity";
    private UsbSerialDevice rfidReader;
    public static String UsbDeviceName;

    //private View mDecorView = getWindow().getDecorView();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //hideSystemUI();
        final Button Contact = (Button) findViewById(R.id.Contact);
        Contact.setOnClickListener(this);
        final Button Settings = (Button) findViewById(R.id.Settings);
        Settings.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        String deviceName = LogUsbDevices(deviceList);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        if (!deviceName.equals("none")) {
            UsbDevice device = deviceList.get(deviceName);
            UsbDeviceName = deviceName;
            manager.requestPermission(device, mPermissionIntent);
            UsbDeviceConnection connection = manager.openDevice(device);
            Log.d(TAG, "Connection: " + connection.toString());
            rfidReader = UsbSerialDevice.createUsbSerialDevice(device, connection);
            Log.d(TAG, UsbSerialDebugger.ENCODING);

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

        } else {
            Log.d(TAG, "no viable target device was found");
        }


    }

    // This snippet hides the system bars.
//    private void hideSystemUI() {
//        // Set the IMMERSIVE flag.
//        // Set the content to appear under the system bars so that the content
//        // doesn't resize when the system bars hide and show.
//        mDecorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
//    }


    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] bytes) {
            try {
                if (bytes.length > 2) {
                    rfidReader.close();

                    boolean AccessGranted = DatabaseConnector.EmployeeAuthorized(bytes);
                    Log.d(TAG, AccessGranted ? "True" : "False");
                    if (AccessGranted) {
                        Log.d(TAG, "employee authorized");
                        Intent intent = new Intent(MainActivity.this, CheckActivity.class);
                        MainActivity.this.startActivity(intent);//go to PPE
                    } else {
                        Log.d(TAG, "employee denied");
                        Intent i = new Intent(MainActivity.this, DeniedActivity.class);
                        //Intent i = new Intent(MainActivity.this, CheckActivity.class);
                        MainActivity.this.startActivity(i);//go to access denied
                    }
                    // Toast.makeText(MainActivity.this, "read data: " + readString, Toast.LENGTH_SHORT).show();
                    //query
                } else {
                    //Log.v(TAG, "parity bits: " + bytes.toString());
                }

            } catch (SQLException | ClassNotFoundException | UnsupportedEncodingException se) {
                se.printStackTrace();
            }
        }
    };

    private String LogUsbDevices(HashMap<String, UsbDevice> deviceList) {
        int UsbIndex = 0;
        String targetDevice = "none";
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        Log.d(TAG, "Devices: " + deviceList.keySet());
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            if (device.getProductId() == 24577) {
                targetDevice = device.getDeviceName();
            }


            Log.d(TAG, "Device " + UsbIndex + ": " + device);
            UsbIndex++;

        }



        return targetDevice;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Contact:
                Intent contact = new Intent(this, TechContact.class);
                contact.putExtra("return", "MainActivity");
                this.startActivity(contact);
                break;
            case R.id.Settings:
                //this.startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
    }
}


/***
 *   this function will allow the use of an intent filter to open the app on connection to USB

 */
//    private final BroadcastReceiver mUSBReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (ACTION_USB_PERMISSION.equals(action)) {
//                synchronized (this) {
//                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                        if (device != null) {
//
//                        }
//                    } else {
//                        Log.d("UsbPermission", "permissions denied for " + device);
//                    }
//                }
//            }
//        }
//    };



