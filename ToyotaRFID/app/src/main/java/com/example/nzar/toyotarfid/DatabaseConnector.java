//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static com.example.nzar.toyotarfid.SettingsActivity.settings;

/**
 * Created by cravers on 6/29/2017.
 */

/*
The Database Connector class is where all interactions with the designated database for this project will happen
This class grabs the data for the equipment being used, the person who is trying to badge in, the ppe requirements
and is also responsible for sending the appropriate data back to the database for keeping logs
*/
class DatabaseConnector extends AppCompatActivity {
//all variables are undergoing review pending changes
//    private static final String TAG = "DBConnectorLib"; //set Logging tag
//    public static LabPerson currentLabPerson = new LabPerson();
//    public static Equipment currentEquipment = new Equipment();
//    public static ArrayList<Integer> LabTechBadgeNumbers = new ArrayList<>();
//    public static SparseArray<String> LabPersonEmailList = new SparseArray<>();
//    public static ArrayList<String> PPEList = new ArrayList<>();
//    public  static ArrayList<Integer> CertIDs = new ArrayList<>();
//    static SharedPreferences settings;
//    private static String dbUrl;
//    private static String dbPort;
//    private static String dbUser;
//    private static String dbPasswd;
//    private static String dbName;
//    private static String dbEngine;
//    private static java.sql.Timestamp logIn;

    //class to store information on person signing in
//    static class LabPerson {
//        int ID;
//        boolean Authorized;
//        int OverrideID;
//    }
//
    //information on the equipment is stored here
//    private static class Equipment {
//        int EquipID;
//        int LMSCertID;
//        String IP;
//
//    }


    //sets the log in time
//    public static void setTime() {
//        java.util.Date login = new java.util.Date();
//        logIn = new java.sql.Timestamp(login.getTime());
//    }


    //sets up the current equipment that this machine in running on | equipment is undergoing changes
//    static void setCurrentEquipment() {
//        Equipment equip = new Equipment();
//        equip.EquipID = settings.getInt("EquipID", 0);
//        equip.IP = settings.getString("static_ip", "192.168.0.235");
//        equip.LMSCertID = settings.getInt("LMSCertID", 0);
//        currentEquipment = equip;
//    }

    //sets information for whoever is trying to badge in
//    static void setCurrentEmployee(LabPerson currentEmployee) {
//        DatabaseConnector.currentLabPerson = currentEmployee;
//    }

    //default values for database connectivity | settings are undergoing changes
//    static void setSettings(SharedPreferences settings) {
//        DatabaseConnector.settings = settings;
//        DatabaseConnector.dbUrl = settings.getString("db_url", "192.168.0.200");
//        DatabaseConnector.dbPort = settings.getString("db_port", "3306");
//        DatabaseConnector.dbUser = settings.getString("db_user", "Connor");
//        DatabaseConnector.dbPasswd = settings.getString("db_pw", "password");
//        DatabaseConnector.dbName = settings.getString("db_name", "toyotamockupfinal");
//        DatabaseConnector.dbEngine = settings.getString("db_engine", "mysql");
//
//    }


//give the badge number as a string, provide progress messages as Strings, and return a Boolean if the user is allowed
    public class TILTPostUserTask extends AsyncTask<String, String, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {

            return false;
        }
    }

    public class TILTGetUserTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
    }

    public class TILTGetTechTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
    }

    public class SetNetworkConfig extends AsyncTask<Void,Void,Boolean> {
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
                NetworkConfigurator.setIpAssignment("STATIC", wifiConf); //or "DHCP" for dynamic setting
                NetworkConfigurator.setIpAddress(InetAddress.getByName(settings.getString("static_ip", "192.168.0.235")), 24, wifiConf);
                NetworkConfigurator.setDNS(InetAddress.getByName("8.8.8.8"), wifiConf);
                wifiManager.updateNetwork(wifiConf); //apply the setting
                wifiManager.saveConfiguration(); //Save it
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }



    @SuppressWarnings("unchecked")
    static class NetworkConfigurator {

        static void setIpAssignment(String assign, WifiConfiguration wifiConf)
                throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
            setEnumField(wifiConf, assign, "ipAssignment");
        }

        static void setIpAddress(InetAddress addr, int prefixLength, WifiConfiguration wifiConf)
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

        static void setDNS(InetAddress dns, WifiConfiguration wifiConf)
                throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
            Object linkProperties = getField(wifiConf, "linkProperties");
            if (linkProperties == null) return;

            ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(linkProperties, "mDnses");
            mDnses.clear(); //or add a new dns address , here I just want to replace DNS1
            mDnses.add(dns);
        }

        static Object getField(Object obj, String name)
                throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
            Field f = obj.getClass().getField(name);
            return f.get(obj);
        }

        static Object getDeclaredField(Object obj, String name)
                throws SecurityException, NoSuchFieldException,
                IllegalArgumentException, IllegalAccessException {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(obj);
        }

        static void setEnumField(Object obj, String value, String name)
                throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
            Field f = obj.getClass().getField(name);
            f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
        }

    }


}