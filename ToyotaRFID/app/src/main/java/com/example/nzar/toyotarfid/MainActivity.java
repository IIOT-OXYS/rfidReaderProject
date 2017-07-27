package com.example.nzar.toyotarfid;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.felhr.usbserial.UsbSerialDebugger;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.sql.*;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String ACTION_USB_PERMISSION = "com.android.example.nzar.toyotarfid.USB_PERMISSION";
    private final String TAG = "MainActivity";
    private UsbSerialDevice rfidReader;
    private UsbSerialDevice relayController;
    public static String relayDeviceName = null;
    public static String rfidDeviceName = null;

    //private View mDecorView = getWindow().getDecorView();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //hideSystemUI();
        final Button Contact = (Button) findViewById(R.id.Contact);

        Contact.setOnClickListener(this);

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

        if (rfidDeviceName == null || relayDeviceName == null) {
            LogUsbDevices(deviceList);
        }

        if (rfidDeviceName != null) {
            rfidReader = attachUsbSerial(rfidDeviceName, deviceList, manager);

            if (rfidReader != null) {
                rfidReader.setBaudRate(9600);
                rfidReader.setDataBits(UsbSerialInterface.DATA_BITS_8);
                rfidReader.setStopBits(UsbSerialInterface.STOP_BITS_1);
                rfidReader.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                rfidReader.setParity(UsbSerialInterface.PARITY_ODD);
                rfidReader.setDTR(false);
                rfidReader.setRTS(false);
                rfidReader.read(mCallback); //this registers the device to a threaded callback
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getPointerCount() == 4) {
            if (rfidReader != null) rfidReader.close();
            this.startActivity(new Intent(this, SettingsActivity.class));
            return super.onTouchEvent(event);

        } else {
            return super.onTouchEvent(event);
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


    /*
        This threaded callback is where we read any data coming back from our device.
        In our case, the reader gives three distinct messages, where the data of interest
        is padded by some sets of parity bits. For our case we filter out those parity bits
        and listen only for messages above a certain length.
        Then we use the DatabaseConnector class to handle the query and find the employee that
        the badgeNumber belongs to.
     */
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] bytes) {
            try {
                if (bytes.length > 2) {
                    rfidReader.close();
                    String badgeNumber = new String(bytes, "ASCII").trim();

                    boolean AccessGranted = DatabaseConnector.EmployeeAuthorized(badgeNumber);
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
                }

            } catch (SQLException | ClassNotFoundException | UnsupportedEncodingException se) {
                se.printStackTrace();
            }
        }
    };


    /*
        This method will iterate and log all attached USB devices, and filter them for known-working
        PIDs. Then we return the key for that device to access it from the HashMap.
     */
    private void LogUsbDevices(HashMap<String, UsbDevice> deviceList) {
        int UsbIndex = 0;
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        Log.d(TAG, "Devices: " + deviceList.keySet());
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            if (device.getProductId() == 24577) {
                rfidDeviceName = device.getDeviceName();
                Log.d(TAG, "rfid device recognized");
            } else if (device.getProductId() == 1155) {
                relayDeviceName = device.getDeviceName();
                Log.d(TAG, "relay device recognized");
            }


            Log.d(TAG, "Device " + UsbIndex + ": " + device);
            UsbIndex++;

        }


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




    /*
        Standard onClick to move between activities using the buttons.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Contact:
                if (rfidReader != null) {
                    rfidReader.close();
                }
                Intent contact = new Intent(this, TechContact.class);
                contact.putExtra("return", "MainActivity");
                this.startActivity(contact);
                break;
        }
    }
}


/*
    this function will allow the use of an intent filter to open the app on connection to USB.

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



