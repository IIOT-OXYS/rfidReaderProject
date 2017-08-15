//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    public static ArrayList<String> PPEList = new ArrayList<>();
    public static ArrayList<LabTech> LabTechList = new ArrayList<>();
    public static int currentSessionID;

    public static class LabTech{
        int LabTechID;
        String firstName;
        String lastName;
        String email;
        String phoneNumber;
      }

      public static LabTech clearTech(LabTech temp){
        LabTech newTemp = new LabTech();
         return newTemp;

    }
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


    private static JsonReader TILTAPITask(URL url, String method) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", "basic VElMVFdlYkFQSToxM1RJTFRXZWJBUEkxMw==");

        if (connection.getResponseCode() == 201 || connection.getResponseCode() == 200) {
            InputStream RawResponse = connection.getInputStream();
            InputStreamReader Response = new InputStreamReader(RawResponse, "UTF-8");
            connection.disconnect();
            return new JsonReader(Response);


        } else {
            throw new Exception("bad http response ");
        }
    }


//give the badge number as a string, provide progress messages as Strings, and return a Boolean if the user is allowed
    public static class TILTPostUserTask extends AsyncTask<String, String, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String machineIP = "";
            String badgeID = params[0];
            String isLoggingOut, sessionID;
            boolean UserAuthorized = false;
            if (params[1] != null) {
                sessionID = params[1];
                isLoggingOut = "true";
            } else {
                currentSessionID = new Random().nextInt();
                sessionID = String.valueOf(currentSessionID);
                isLoggingOut = "false";
            }

            try {
                URL url = new URL("http://V01DES168.tmm.na.corp.toyota.com/tiltwebapi/api/Users?" +
                        "sessionID=" + sessionID +
                        "&machineIP=" + machineIP +
                        "&badgeID=" + badgeID +
                        "&isLoggingOut=" + isLoggingOut);



                JsonReader Response = TILTAPITask(url,"POST");
                if(Response.hasNext()) {
                    PPEList.clear();
                    while (Response.hasNext()) {
                        //parse response for PPE info
                        //if the response is not empty, set UserAuthorized to true
                        String key = Response.nextName();
                        if (key.equals("PPE")) {
                            PPEList.add(Response.nextString());
                        } else {
                            Response.skipValue();
                        }
                    }
                }
                else{
                    Response.close();
                    UserAuthorized = false;
                    return UserAuthorized;
                }
                Response.close();
                UserAuthorized = true;
                return UserAuthorized;




            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;





        }
    }



    public static class TILTPostTechTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String sessionID="";
            String machineIP="";
            String content = "";//content of the email message
            try {
                URL url = new URL("http://V01DES168.tmm.na.corp.toyota.com/tiltwebapi/api/Technicians" +
                        "?sessionID=" + sessionID +
                        "&machineIP="+ machineIP +
                        "&content="+ content);

                TILTAPITask(url, "POST");


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class TILTGetTechTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://V01DES168.tmm.na.corp.toyota.com/tiltwebapi/api/Technicians");
                JsonReader ResponseReader = TILTAPITask(url, "GET");
                LabTechList.clear();
                LabTech temp = new LabTech();
                while (ResponseReader.hasNext()) {
                    /*
                    parse response for tech info
                    each element of the array contains:
                    int LabTechID
                    String FirstName
                    String LastName
                    String Email
                    String PhoneNumber
                     */
                    String key = ResponseReader.nextName();
                    switch (key){
                        case ("LabTechID"):
                            temp.LabTechID = ResponseReader.nextInt();
                            break;
                        case ("FirstName"):
                            temp.firstName = ResponseReader.nextString();
                            break;
                        case  ("LastName"):
                            temp.lastName = ResponseReader.nextString();
                            break;
                        case ("Email"):
                            temp.email = ResponseReader.nextString();
                            break;
                        case ("PhoneNumber"):
                            temp.email = ResponseReader.nextString();
                            LabTechList.add(temp);
                            clearTech(temp);
                            break;
                        default:
                            break;
                    }

                }
                ResponseReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    //take the app context, doesn't provide updates, and return a boolean
    public static class SetNetworkConfigTask extends AsyncTask<Context,Void,Boolean> {
        @Override
        protected Boolean doInBackground(Context... params) {

            WifiConfiguration wifiConf = null;
            WifiManager wifiManager = (WifiManager) params[0].getSystemService(Context.WIFI_SERVICE);
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