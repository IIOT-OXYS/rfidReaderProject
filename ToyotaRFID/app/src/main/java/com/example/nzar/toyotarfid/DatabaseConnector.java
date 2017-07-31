//package name here

package com.example.nzar.toyotarfid;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by cravers on 6/29/2017.
 */

public class DatabaseConnector {

    private static final String TAG = "DBConnectorLib"; //set Logging tag

    /*
        These private strings are the settings used to connect the database, they all have public
        setters to make sure they can be over-written, but can never be read to prevent unauthorized
        database access.
     */
    private static SharedPreferences settings;
    private static String dbUrl;
    private static String dbPort;
    private static String dbUser;
    private static String dbPasswd;
    private static String dbName;
    private static String dbEngine;
    private static String StaticIP;
    private static String SubnetMask;

    private static String WirelessSSID;
    private static String WirelessPasswd;



    //Employee class is to store the information about the employee gathered from the database to minimize database hits
    public static class LabPerson {
        int ID;
        int CertID;
    }

    public static class Equipment{
        int EquipID = 20202020;
        int PPE = 88888888;
        String IP = "192.168.0.235";

    }

    public static LabPerson currentLabPerson;

    private static void setCurrentEmployee(LabPerson currentEmployee) {
        DatabaseConnector.currentLabPerson = currentEmployee;
    }

    static void setSettings(SharedPreferences settings) {
        DatabaseConnector.settings = settings;
        DatabaseConnector.dbUrl = settings.getString("dbUrl", "192.168.0.200");
        DatabaseConnector.dbPort = settings.getString("dbPort", "3306");
        DatabaseConnector.dbUser = settings.getString("dbUser", "Connor");
        DatabaseConnector.dbPasswd = settings.getString("dbPasswd", "password");
        DatabaseConnector.dbName = settings.getString("dbName", "toyotamockupfinal");
        DatabaseConnector.dbEngine = settings.getString("dbEngine", "mysql");
        DatabaseConnector.StaticIP = settings.getString("StaticIP", "192.168.0.235");
        DatabaseConnector.SubnetMask = settings.getString("SubnetMask", "255.255.255.0");
        DatabaseConnector.WirelessSSID = settings.getString("WirelessSSID", "MedSpace");
        DatabaseConnector.WirelessPasswd = settings.getString("WirelessPasswd", "Harvard2MIT");
    }




    /*
        This method will query the database specified to see if the user that just badged in is
        authorized to use the device. It will also set up the currentEmployee to the row obtained.
     */
    static boolean EmployeeAuthorized(int badgeNumber) throws SQLException, ClassNotFoundException, UnsupportedEncodingException {

        /*
            This switch will use the database engine given by the user to establish the connection.
         */
        String dbFullUrl = "";

        switch (dbEngine.toLowerCase().trim()) {
            case "mysql":
                Class.forName("com.mysql.jdbc.Driver");
                dbFullUrl = "jdbc:mysql://" + dbUrl + ":" + dbPort + "/" + dbName;
                break;
            case "postgressql":
                Class.forName("org.postgresql.Driver");
                break;
            case "mssql":
            case "sqlserver":
                Class.forName("com.microsoft.sqlserver.jdbc");
                break;
            case "odbc":
                Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                break;
            default:
                Log.d(TAG, "no driver specified");
                throw new ClassNotFoundException();
        }

        /*
            Try with resources clause will attempt to establish a connection before throwing an exception
         */
        try (Connection connection = DriverManager.getConnection(dbFullUrl, dbUser, dbPasswd)) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT labperson.ID, personcert.LMSCertID FROM labperson"
                                                        + " JOIN personcert ON labperson.ID = personcert.LabPersonID"
                                                        + " WHERE labperson.ID = " + badgeNumber + ";");

            if(results.next()) {


                DatabaseConnector.LabPerson labPerson = new DatabaseConnector.LabPerson();
                DatabaseConnector.Equipment  equipment = new DatabaseConnector.Equipment();
                labPerson.ID = results.getInt(1);
                labPerson.CertID = results.getInt(2);
                DatabaseConnector.setCurrentEmployee(labPerson);
                Statement statement1 = connection.createStatement();
                ResultSet results1 = statement1.executeQuery("SELECT * FROM equipmentcerts WHERE equipmentcerts.EquipmentID = " + equipment.EquipID +
                                                            " AND equipmentcerts.LMSCertID = " + labPerson.CertID + ";");
                if (results1.next()){
                    connection.close();
                    return true;
                }
            }
            else{
                connection.close();
                return false;
            }}
            return false;
    }






    public static void LogDeviceActivity() {

    }

    public static void getDeviceType() {

    }

    public static void getTechContact() {

    }


}