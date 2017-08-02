package com.example.nzar.toyotarfid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/*
MainActivity:
This is the primary class of the application.
This class will initialize the RFID reader and poll it for ID numbers,
which are then parsed and fed into a database query to determine if the user who's ID was tapped
is allowed to use the attached device.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /*
    ID: a string builder that gets the keystrokes from the RFID reader to be parsed and queried
    TAG: the debug tag used in Log statements
     */
    private StringBuilder ID = new StringBuilder();
    private final String TAG = "MainActivity";
    SharedPreferences settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get preferences and set network settings accordingly
        settings = getPreferences(0);

        runSetup(settings);

        //set up buttons with click listeners
        final Button Contact = (Button) findViewById(R.id.Contact);
        Contact.setOnClickListener(this);

        //resets the string builder used to parse the input from the RFID reader.
        if (ID != null) {
            ID.delete(0, ID.length());
        }

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
        if (ID.length() == 0) {//inform the user their tap was registered
            TextView tv = (TextView) findViewById(R.id.main_activity_text);
            tv.setText("Checking certifications. . .");
        }
        if (keyCode == KeyEvent.KEYCODE_BACKSLASH) {//checks for ascii delimiter
            String badgeNumber = ID.toString().trim(); // builds the string from the string builder
            Log.d(TAG, badgeNumber);//log it for debugging

            AsyncTask<String, Integer, Boolean> Job = new DatabaseJob(); //set our custom asynctask
            Job.execute(badgeNumber);//execute the query on a separate thread

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

        if (event.getPointerCount() == 4) {
            SettingsActivity.setSettings(settings);
            this.startActivity(new Intent(this, SettingsActivity.class));
            return super.onTouchEvent(event);

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

    /*
    DatabaseJob:
    This is an implementation of the AsyncTask class, used to perform tasks on alternate threads.
    This implementation uses an Integer input, and provides a Boolean output.
    This allows our database interaction to take place asynchronously, such that it won't make
    the UI thread hang.
     */
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

    private class setEquipmentData extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                DatabaseConnector.setEquipment();
            } catch (SQLException | ClassNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

    }

    private synchronized void runSetup(SharedPreferences settings) {
        if (!settings.getBoolean("hasNetworkConfig", false)) {
            AsyncTask<Void, Void, Boolean> setNetworkJob = new setupNetwork();
            setNetworkJob.execute();
            try {
                if (setNetworkJob.get())
                    settings.edit().putBoolean("hasNetworkConfig", true).apply();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to apply network configuration", Toast.LENGTH_SHORT).show();
            }
        }

        DatabaseConnector.setSettings(settings);
        DatabaseConnector.setCurrentEquipment();


        if (!settings.getBoolean("hasEquipmentData", false)) {
            AsyncTask<Void, Void, Boolean> setEquipment = new setEquipmentData();
            setEquipment.execute();
            try {
                if (setEquipment.get())
                    settings.edit().putBoolean("hasEquipmentData", true).apply();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to get equipment type", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class setupNetwork extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            WifiConfiguration wifiConf = null;
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration conf : configuredNetworks) {
                if (conf.networkId == connectionInfo.getNetworkId()) {
                    wifiConf = conf;
                    break;
                }
            }
            try {
                setIpAssignment("STATIC", wifiConf); //or "DHCP" for dynamic setting
                setIpAddress(InetAddress.getByName(settings.getString("StaticIP", "192.168.0.235")), 24, wifiConf);
                setDNS(InetAddress.getByName("8.8.8.8"), wifiConf);
                wifiManager.updateNetwork(wifiConf); //apply the setting
                wifiManager.saveConfiguration(); //Save it
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }

    public static void setIpAssignment(String assign, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        setEnumField(wifiConf, assign, "ipAssignment");
    }

    public static void setIpAddress(InetAddress addr, int prefixLength, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
            NoSuchMethodException, ClassNotFoundException, InstantiationException, InvocationTargetException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null) return;
        Class laClass = Class.forName("android.net.LinkAddress");
        Constructor laConstructor = laClass.getConstructor(InetAddress.class, int.class);
        Object linkAddress = laConstructor.newInstance(addr, prefixLength);

        ArrayList mLinkAddresses = (ArrayList) getDeclaredField(linkProperties, "mLinkAddresses");
        mLinkAddresses.clear();
        mLinkAddresses.add(linkAddress);
    }

    public static void setGateway(InetAddress gateway, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
            ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null) return;
        Class routeInfoClass = Class.forName("android.net.RouteInfo");
        Constructor routeInfoConstructor = routeInfoClass.getConstructor(InetAddress.class);
        Object routeInfo = routeInfoConstructor.newInstance(gateway);

        ArrayList mRoutes = (ArrayList) getDeclaredField(linkProperties, "mRoutes");
        mRoutes.clear();
        mRoutes.add(routeInfo);
    }

    public static void setDNS(InetAddress dns, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null) return;

        ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(linkProperties, "mDnses");
        mDnses.clear(); //or add a new dns address , here I just want to replace DNS1
        mDnses.add(dns);
    }

    public static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    public static Object getDeclaredField(Object obj, String name)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    public static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }


}



