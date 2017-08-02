//package name here

package com.example.nzar.toyotarfid;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * Created by cravers on 6/29/2017.
 */

class DatabaseConnector extends AppCompatActivity {

    private static final String TAG = "DBConnectorLib"; //set Logging tag

    /*
        These private strings are the settings used to connect the database, they all have public
        setters to make sure they can be over-written, but can never be read to prevent unauthorized
        database access.
     */
    static SharedPreferences settings;
    private static String dbUrl;
    private static String dbPort;
    private static String dbUser;
    private static String dbPasswd;
    private static String dbName;
    private static String dbEngine;




    //Employee class is to store the information about the employee gathered from the database to minimize database hits
    public static class LabPerson {
        int ID;
        int CertID;
    }

    public static class Equipment{
        int EquipID;
        int PPE;
        String IP;

    }
    private static java.sql.Timestamp logIn;

    private static String getFullUrl() throws ClassNotFoundException {
        String dbFullUrl = null;
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
                Log.d(TAG, "driver " + dbEngine + " is not supported");
                throw new ClassNotFoundException();
        }
        return dbFullUrl;
    }

    public static void setTime(){
       java.util.Date login = new java.util.Date();
       java.sql.Timestamp logtime = new java.sql.Timestamp(login.getTime());
        logIn = logtime;
    }

    public static java.sql.Timestamp getTime(){
        return logIn;
    }

    public static LabPerson currentLabPerson;
    public static Equipment currentEquipment;
    public static ArrayList<Integer> LabTechBadgeNumbers;

    static void setCurrentEquipment() {
        Equipment equip = new Equipment();
        equip.EquipID = settings.getInt("EquipID", 0);
        equip.IP = settings.getString("StaticIP", "192.168.0.235");
        equip.PPE = settings.getInt("PPE", 0);
        currentEquipment = equip;
    }


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

    }



    /*
        This method will query the database specified to see if the user that just badged in is
        authorized to use the device. It will also set up the currentEmployee to the row obtained.
     */
    static boolean EmployeeAuthorized(int badgeNumber) throws SQLException, ClassNotFoundException, UnsupportedEncodingException {

        /*
            This switch will use the database engine given by the user to establish the connection.
         */
        String dbFullUrl = getFullUrl();


        /*
            Try with resources clause will attempt to establish a connection before throwing an exception
         */
            try (Connection connection = DriverManager.getConnection(dbFullUrl, dbUser, dbPasswd)) {
                Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery("SELECT labperson.ID, personcert.LMSCertID FROM labperson"
                        + " JOIN personcert ON labperson.ID = personcert.LabPersonID"
                        + " WHERE labperson.ID = " + badgeNumber + ";");
                DatabaseConnector.LabPerson labPerson = new DatabaseConnector.LabPerson();
                labPerson.ID = badgeNumber;
                DatabaseConnector.setCurrentEmployee(labPerson);
                if (results.next()) {

                    labPerson.ID = results.getInt(1);
                    labPerson.CertID = results.getInt(2);
                    DatabaseConnector.setCurrentEmployee(labPerson);
                    Statement statement1 = connection.createStatement();
                    ResultSet results1 = statement1.executeQuery("SELECT * FROM equipmentcerts WHERE equipmentcerts.EquipmentID = " + currentEquipment.EquipID +
                            " AND equipmentcerts.LMSCertID = " + labPerson.CertID + ";");
                    if (results1.next()) {
                        connection.close();
                        return true;
                    }
                } else {
                    connection.close();
                    return false;
                }
            }
            return false;
        }


    static void insertData() throws SQLException, ClassNotFoundException, UnsupportedEncodingException{
        java.util.Date login = new java.util.Date();
        java.sql.Timestamp logtime = new java.sql.Timestamp(login.getTime());
        String dbFullUrl = getFullUrl();
        try (Connection connection = DriverManager.getConnection(dbFullUrl, dbUser, dbPasswd)) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO lablog " +
                                                    "(LogID, Login, SessionID, Logout, AccessDenied, BadgeID, EquipmentID)" +
                                                        "VALUES (?, ?, ?, ?, ?, ?, ?)");
            int logID = uniqueID();
            int badge = currentLabPerson.ID;
            String session = UUID.randomUUID().toString();
            session = session.replaceAll("-", "");
            preparedStatement.setInt(1,logID);
            preparedStatement.setTimestamp(2, logIn);
            preparedStatement.setString(3, session);
            preparedStatement.setTimestamp(4, logtime);
            preparedStatement.setBoolean(5, false);
            preparedStatement.setInt(6, badge);
            preparedStatement.setInt(7, currentEquipment.EquipID);
            preparedStatement.executeUpdate();

            connection.close();
        }

    }
    static int uniqueID() throws SQLException, ClassNotFoundException, UnsupportedEncodingException{
        Random rand = new Random();
        int n = rand.nextInt(1999999999);
        String dbFullUrl = getFullUrl();
        try (Connection connection = DriverManager.getConnection(dbFullUrl, dbUser, dbPasswd)) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT * FROM lablog WHERE lablog.LogID = " + n + ";");
            if(!results.next()) {
                connection.close();
                return n;
            }
            else{
                connection.close();
                uniqueID();
            }
        }
            return n;
    }

    static void deniedData() throws SQLException, ClassNotFoundException, UnsupportedEncodingException{
        java.util.Date login = new java.util.Date();
        java.sql.Timestamp logtime = new java.sql.Timestamp(login.getTime());
        String dbFullUrl = getFullUrl();
        try (Connection connection = DriverManager.getConnection(dbFullUrl, dbUser, dbPasswd)) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO lablog " +
                    "(LogID, Login, SessionID, Logout, AccessDenied, BadgeID, EquipmentID)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)");
            int logID = uniqueID();
            int badge = currentLabPerson.ID;
            boolean x = true;
            String session = UUID.randomUUID().toString();
            session = session.replaceAll("-", "");
            preparedStatement.setInt(1, logID);
            preparedStatement.setTimestamp(2, logtime);
            preparedStatement.setString(3, session);
            preparedStatement.setTimestamp(4, null);
            preparedStatement.setBoolean(5, x);
            preparedStatement.setInt(6, badge);
            preparedStatement.setInt(7, currentEquipment.EquipID);
            preparedStatement.executeUpdate();
            connection.close();

        }
    }
    public static void setEquipment() throws SQLException, ClassNotFoundException, UnsupportedEncodingException{
        String dbFullUrl = getFullUrl();
        try (Connection con = DriverManager.getConnection(dbFullUrl, dbUser, dbPasswd)) {
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery("SELECT equipment.EquipmentID, equipmentppe.PPEID FROM equipment"
                    + " JOIN equipmentppe ON equipment.EquipmentID = equipmentppe.EquipmentID"
                    + " WHERE equipment.IPAddress = " + "\"" + currentEquipment.IP + "\"" + ";");
            res.next();
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("EquipID", res.getInt(1));
            editor.putInt("PPE", res.getInt(2));
            editor.apply();
            DatabaseConnector.setCurrentEquipment();

            con.close();
        }
    }

    public static String getPPE() throws SQLException, ClassNotFoundException, UnsupportedEncodingException{
        String dbFullUrl = getFullUrl();
        try(Connection con = DriverManager.getConnection(dbFullUrl,dbUser, dbPasswd)){
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery("SELECT ppe.PPE FROM ppe WHERE ppe.PPEID = " + currentEquipment.PPE + ";");
            res.next();
            String PPE = res.getString(1);
            return PPE;

        }
    }

    public static void fillLabTech() throws SQLException, ClassNotFoundException, UnsupportedEncodingException {
        String dbFullUrl = getFullUrl();
        try (Connection con = DriverManager.getConnection(dbFullUrl, dbUser, dbPasswd)) {
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery("SELECT labtech.LabTech_BadgeID FROM labtech;");
            int i = 1;
            while (res.next()) {
                LabTechBadgeNumbers.add(res.getInt(i));
                i++;
            }
        }
    }
    public static void LogDeviceActivity() {

    }

    public static void getDeviceType() {

    }

    public static void getTechContact() {

    }



}