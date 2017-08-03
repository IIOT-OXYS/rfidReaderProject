//package name here

package com.example.nzar.toyotarfid;

import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
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
    public static LabPerson currentLabPerson = new LabPerson();
    public static Equipment currentEquipment = new Equipment();
    public static ArrayList<Integer> LabTechBadgeNumbers = new ArrayList<>();
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
    private static java.sql.Timestamp logIn;

    public static void setTime() {
        java.util.Date login = new java.util.Date();
        logIn = new java.sql.Timestamp(login.getTime());
    }

    static void setCurrentEquipment() {
        Equipment equip = new Equipment();
        equip.EquipID = settings.getInt("EquipID", 0);
        equip.IP = settings.getString("static_ip", "192.168.0.235");
        equip.PPE = settings.getInt("PPE", 0);
        currentEquipment = equip;
    }

     static void setCurrentEmployee(LabPerson currentEmployee) {
        DatabaseConnector.currentLabPerson = currentEmployee;
    }

    static void setSettings(SharedPreferences settings) {
        DatabaseConnector.settings = settings;
        DatabaseConnector.dbUrl = settings.getString("db_url", "192.168.0.200");
        DatabaseConnector.dbPort = settings.getString("db_port", "3306");
        DatabaseConnector.dbUser = settings.getString("db_user", "Connor");
        DatabaseConnector.dbPasswd = settings.getString("db_pw", "password");
        DatabaseConnector.dbName = settings.getString("db_name", "toyotamockupfinal");
        DatabaseConnector.dbEngine = settings.getString("db_engine", "mysql");

    }

    /*
        This method will query the database specified to see if the user that just badged in is
        authorized to use the device. It will also set up the currentEmployee to the row obtained.
     */
    static boolean getEmployeeAuthorization(int badgeNumber) throws SQLException, ClassNotFoundException, UnsupportedEncodingException {

        String dbFullUrl = generateFullUrl();
        DatabaseConnector.LabPerson labPerson = new DatabaseConnector.LabPerson();
        labPerson.ID = badgeNumber;


        try (Connection connection = DriverManager.getConnection(dbFullUrl, dbUser, dbPasswd)) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT labperson.ID, personcert.LMSCertID FROM labperson"
                    + " JOIN personcert ON labperson.ID = personcert.LabPersonID"
                    + " WHERE labperson.ID = " + badgeNumber + ";");
            if (results.next()) {

                labPerson.CertID = results.getInt(2);
                Statement statement1 = connection.createStatement();
                ResultSet results1 = statement1.executeQuery("SELECT * FROM equipmentcerts WHERE equipmentcerts.EquipmentID = " + currentEquipment.EquipID +
                        " AND equipmentcerts.LMSCertID = " + labPerson.CertID + ";");
                if (results1.next()) {
                    labPerson.Authorized = true;
                    setCurrentEmployee(labPerson);
                    connection.close();
                    return true;
                }
            } else {
                labPerson.Authorized = false;
                setCurrentEmployee(labPerson);
                connection.close();
                return false;
            }
        }
        labPerson.CertID = 0;
        labPerson.Authorized = false;
        setCurrentEmployee(labPerson);

        return false;
    }

    static void insertLoginData() throws SQLException, ClassNotFoundException, UnsupportedEncodingException {
        java.util.Date login = new java.util.Date();
        java.sql.Timestamp logtime = new java.sql.Timestamp(login.getTime());
        String dbFullUrl = generateFullUrl();
        try (Connection connection = DriverManager.getConnection(dbFullUrl, dbUser, dbPasswd)) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO lablog " +
                    "(LogID, Login, SessionID, Logout, AccessDenied, BadgeID, EquipmentID, OverrideBadgeID)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            int logID = generateUniqueID();

            String session = UUID.randomUUID().toString();
            session = session.replaceAll("-", "");
            preparedStatement.setInt(1, logID);
            preparedStatement.setTimestamp(2, currentLabPerson.Authorized ? logIn : logtime);
            preparedStatement.setString(3, session);
            preparedStatement.setTimestamp(4, currentLabPerson.Authorized ? logtime : null);
            preparedStatement.setBoolean(5, !currentLabPerson.Authorized);
            preparedStatement.setInt(6, currentLabPerson.ID);
            preparedStatement.setInt(7, currentEquipment.EquipID);
            preparedStatement.setInt(8, currentLabPerson.OverrideID);
            preparedStatement.executeUpdate();

            connection.close();
        }

    }

    static int generateUniqueID() throws SQLException, ClassNotFoundException, UnsupportedEncodingException {
        Random rand = new Random();
        int n = rand.nextInt(1999999999);
        String dbFullUrl = generateFullUrl();
        try (Connection connection = DriverManager.getConnection(dbFullUrl, dbUser, dbPasswd)) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT * FROM lablog WHERE lablog.LogID = " + n + ";");
            if (!results.next()) {
                connection.close();
                return n;
            } else {
                connection.close();
                generateUniqueID();
            }
        }
        return n;
    }

    public static void setEquipment() throws SQLException, ClassNotFoundException, UnsupportedEncodingException {
        String dbFullUrl = generateFullUrl();
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

    public static String getPPE() throws SQLException, ClassNotFoundException, UnsupportedEncodingException {
        String dbFullUrl = generateFullUrl();
        try (Connection con = DriverManager.getConnection(dbFullUrl, dbUser, dbPasswd)) {
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery("SELECT ppe.PPE FROM ppe WHERE ppe.PPEID = " + currentEquipment.PPE + ";");
            res.next();
            return res.getString(1);

        }
    }

    public static void fillLabTech() throws SQLException, ClassNotFoundException, UnsupportedEncodingException {
        String dbFullUrl = generateFullUrl();
        try (Connection con = DriverManager.getConnection(dbFullUrl, dbUser, dbPasswd)) {
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery("SELECT labtech.LabTech_BadgeID FROM labtech;");
            while (res.next()) {
                LabTechBadgeNumbers.add(res.getInt(1));
            }
        }
    }


    private static String generateFullUrl() throws ClassNotFoundException {
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

    static class LabPerson {
        int ID;
        int CertID;
        boolean Authorized;
        int OverrideID;
    }

    private static class Equipment {
        int EquipID;
        int PPE;
        String IP;

    }

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
            Object out = f.get(obj);
            return out;
        }

        static Object getDeclaredField(Object obj, String name)
                throws SecurityException, NoSuchFieldException,
                IllegalArgumentException, IllegalAccessException {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            Object out = f.get(obj);
            return out;
        }

        static void setEnumField(Object obj, String value, String name)
                throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
            Field f = obj.getClass().getField(name);
            f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
        }

    }


}