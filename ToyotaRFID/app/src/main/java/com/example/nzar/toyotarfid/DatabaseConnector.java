//package name here

package com.example.nzar.toyotarfid;

import android.content.Context;
import android.test.mock.MockContext;
import android.util.Log;
import android.widget.Toast;
import java.util.Date;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.Random;
import java.util.UUID;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;

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
    private static String DatabaseRoot = "jdbc:mysql://192.168.0.200:3306/toyotamockupfinal";
    private static String DatabaseUser = "Connor";
    private static String DatabasePasswd = "password";
    private static String authorizationProceedure = null;
    private static String loggingProceedure = null;
    private static String machineType = null;
    private static String[] Schemas = {"dummyemployee"};
    private static String EmployeeTable = "employeeinfo";
    private static String LoggingTable = "";
    private static String TechTable = "";

    private enum Engine {MySQL, PostGreSQL, SQLServer, ODBC}

    private static Engine engine = Engine.MySQL;

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

    private static LabPerson currentLabPerson;
    private static java.sql.Timestamp logIn;

    public static void setTime(){
       java.util.Date login = new java.util.Date();
       java.sql.Timestamp logtime = new java.sql.Timestamp(login.getTime());
        logIn = logtime;
    }

    public static java.sql.Timestamp getTime(){
        return logIn;
    }

    private static void setCurrentEmployee(LabPerson currentEmployee) {
        DatabaseConnector.currentLabPerson = currentEmployee;
    }

    public static LabPerson getCurrentLabPerson() {
        return currentLabPerson;
    }


    public static void setEngine(Engine engine) {
        DatabaseConnector.engine = engine;
    }

    public static void setAuthorizationProceedure(String authorizationProceedure) {
        DatabaseConnector.authorizationProceedure = authorizationProceedure;
    }

    public static void setDatabasePasswd(String databasePasswd) {
        DatabasePasswd = databasePasswd;
    }

    public static void setDatabaseRoot(String databaseRoot) {
        DatabaseRoot = databaseRoot;
    }

    public static void setDatabaseUser(String databaseUser) {
        DatabaseUser = databaseUser;
    }

    public static void setEmployeeTable(String employeeTable) {
        EmployeeTable = employeeTable;
    }

    public static void setLoggingProceedure(String loggingProceedure) {
        DatabaseConnector.loggingProceedure = loggingProceedure;
    }

    public static void setLoggingTable(String loggingTable) {
        LoggingTable = loggingTable;
    }

    public static void setMachineType(String machineType) {
        DatabaseConnector.machineType = machineType;
    }

    public static void setSchemas(String[] schemas) {
        Schemas = schemas;
    }

    public static void setTechTable(String techTable) {
        TechTable = techTable;
    }


    /*
        This method will query the database specified to see if the user that just badged in is
        authorized to use the device. It will also set up the currentEmployee to the row obtained.
     */
    static boolean EmployeeAuthorized(int badgeNumber) throws SQLException, ClassNotFoundException, UnsupportedEncodingException {

        /*
            This switch will use the database engine given by the user to establish the connection.
         */
        switch (engine) {
            case MySQL:
                Class.forName("com.mysql.jdbc.Driver");
                break;
            case PostGreSQL:
                Class.forName("org.postgresql.Driver");
                break;
            case SQLServer:
                Class.forName("com.microsoft.sqlserver.jdbc");
                break;
            case ODBC:
                Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                break;
            default:
                Log.d(TAG, "no driver specified");
                throw new ClassNotFoundException();
        }

        /*
            Try with resources clause will attempt to establish a connection before throwing an exception
         */
        try (Connection connection = DriverManager.getConnection(DatabaseRoot, DatabaseUser, DatabasePasswd)) {
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

    static void insertData() throws SQLException, ClassNotFoundException, UnsupportedEncodingException{
        java.util.Date login = new java.util.Date();
        java.sql.Timestamp logtime = new java.sql.Timestamp(login.getTime());
        DatabaseConnector.Equipment equipment = new DatabaseConnector.Equipment();
        try (Connection connection = DriverManager.getConnection(DatabaseRoot, DatabaseUser, DatabasePasswd)) {
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
            preparedStatement.setInt(7, equipment.EquipID);

        }

    }
    static int uniqueID() throws SQLException, ClassNotFoundException, UnsupportedEncodingException{
        Random rand = new Random();
        int n = rand.nextInt(1999999999);
        try (Connection connection = DriverManager.getConnection(DatabaseRoot, DatabaseUser, DatabasePasswd)) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT * FROM lablog WHERE lablog.LogID = " + n + ";");
            if(!results.next()) {
            return n;
            }
            else{
                uniqueID();
            }
        }
            return n;
    }
    public static void LogDeviceActivity() {

    }

    public static void getDeviceType() {

    }

    public static void getTechContact() {

    }



}