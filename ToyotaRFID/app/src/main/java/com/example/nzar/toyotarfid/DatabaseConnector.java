//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

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

    /*
        This method will query the database specified to see if the user that just badged in is
        authorized to use the device. It will also set up the currentEmployee to the row obtained.
     */

    public class TILTAPIPostUser extends AsyncTask<Integer, String, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {

            return false;
        }
    }


}