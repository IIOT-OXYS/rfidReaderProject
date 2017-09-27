//Written by Skyelar Craver and Connor Brennan
//OXYS Corp
//2017
package com.example.nzar.toyotarfid;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by cravers on 6/29/2017.
 */

/*
The Database Connector class is where all interactions with the designated database for this project will happen
This class grabs the data for the equipment being used, the person who is trying to badge in, the ppe requirements
and is also responsible for sending the appropriate data back to the database for keeping logs
*/
class DatabaseConnector extends AppCompatActivity {

    public static ArrayList<PPE> PPEList = new ArrayList<>();
    public static ArrayList<LabTech> LabTechList = new ArrayList<>();
    public static int currentSessionID;
    public static String machineID;
    public static String baseServerUrl;
    public static String currentBadgeID = "";

    static class LabTech {
        int LabTechID = -1;
        String firstName = null;
        String lastName = null;
        String email = null;
        String phoneNumber = null;
        Drawable Image = null;
    }

    static class PPE {
        int PPEID = -1;
        String name = null;
        Drawable Image = null;
        boolean Required = false;
        boolean Restricted = false;
    }

    public static boolean BindPreferences(SharedPreferences prefs) {
        machineID = prefs.getString("machineID", null);
        baseServerUrl = prefs.getString("baseServerUrl", null);

        return (machineID == null || baseServerUrl == null);
    }


    @Nullable
    private static synchronized JsonReader TILTAPITask(HttpURLConnection connection, String method) throws Exception {
        final String TILT_API_KEY = "basic VElMVFdlYkFQSToxM1RJTFRXZWJBUEkxMw==";

        Log.d("TILTAPI", "Using key: " + TILT_API_KEY);
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", TILT_API_KEY);
        int ResponseCode = connection.getResponseCode();
        Log.d("TILTAPI", "Respose code: " + String.valueOf(ResponseCode));

        if (ResponseCode < 400) {
            Log.d("TILTAPI", "Received valid response");
            InputStream RawResponse = connection.getInputStream();
            InputStreamReader Response = new InputStreamReader(RawResponse, "UTF-8");
            return new JsonReader(Response);


        } else {
            Log.d("TILTAPI", "Response Code indicated error, JSON parsing will be skipped");
            connection.disconnect();
            return null;
        }

    }

    private static synchronized Drawable ImageParser(String jsonImage) {
        try {
            if (jsonImage != null) {

                jsonImage = jsonImage.substring(jsonImage.indexOf(',')+1);
                Log.d("TILTJSON", "Parsing image...");
                byte encodedImage[] = Base64.decode(jsonImage, Base64.DEFAULT);
                int length = encodedImage.length;
                Log.d("TILTJSON", "decoding " + String.valueOf(length) + "bytes into image");
                Bitmap bitmap = BitmapFactory.decodeByteArray(encodedImage, 0, length);
                if (bitmap != null) {
                    Log.d("TILTJSON", "Image parsed successfully");
                    return new BitmapDrawable(Resources.getSystem(), bitmap);
                } else {
                    Log.d("TILTJSON", "Image field did not contain a valid image");
                    return null;
                }
            } else {
                Log.d("TILTJSON", "Image field was null");
                return null;
            }
        } catch (Exception e) {
            Log.d("TILTJSON", "Image parsing failed, printing stack trace:");
            e.printStackTrace();
            return null;
        }

    }

    //give the badge number as a string, provide progress messages as Strings, and return a Boolean if the user is allowed
    static class TILTPostUserTask extends AsyncTask<String, String, String> {
        final String TAG = "TILTPOSTUser";
        int sessionID = -1;
        boolean isLoggingOut = false;

        void setLoggingOut(boolean loggingOut) {
            isLoggingOut = loggingOut;
        }

         void setSessionID(int sessionID) {
            this.sessionID = sessionID;
        }

        Context context;

         interface OnFinishedParsingListener{
            void onFinishedParsing(TILTPostUserTask Job);
        }
         OnFinishedParsingListener onFinishedParsingListener;

         void setOnFinishedParsingListener(OnFinishedParsingListener onFinishedParsingListener) {
            this.onFinishedParsingListener = onFinishedParsingListener;
        }

        @Override
        protected void onPostExecute(String aString) {
            if (onFinishedParsingListener != null) {
                onFinishedParsingListener.onFinishedParsing(this);
            }
            super.onPostExecute(aString);

        }

        public void setContext(Context ctx) {
            context = ctx;
        }

        @Override
        protected synchronized String doInBackground(String... params) {

            String badgeID = params[0];

            currentSessionID = sessionID;

            final String APIConnectionUrl = "http://" +
                    baseServerUrl +
                    "/TILTWebApi/api/Users" +
                    "?sessionID=" + String.valueOf(sessionID) +
                    "&machineIP=" + machineID +
                    "&badgeID=" + badgeID +
                    "&isLoggingOut=" + String.valueOf(isLoggingOut);


            try {

                URL url = new URL(APIConnectionUrl);
                Log.d(TAG, url.toString());

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();


                JsonReader Response = TILTAPITask(connection, "POST");

                if (Response == null) {
                    Log.d(TAG, "JSON response was null");
                    return "Exception";
                }
                if (isLoggingOut) {
                    Log.d(TAG, "Received valid response for logout request, user will be logged out.");
                    return "Logout";
                }
                currentBadgeID = badgeID;
                boolean UserHasCerts = false, UserIsTech = false, MachineNeedsTech = false;
                Response.beginObject();
                while (Response.hasNext()) {
                    if (Response.peek() != JsonToken.NULL) {
                        switch (Response.nextName()) {
                            case "MachinePPE":
                                if (Response.peek() == JsonToken.BEGIN_ARRAY) {
                                    Response = PPEJsonParse(Response);
                                } else {
                                    Response.skipValue();
                                }
                                Log.d(TAG, "Found information for " + String.valueOf(DatabaseConnector.PPEList.size()) + "PPEs");
                                break;
                            case "UserHasCerts":
                                UserHasCerts = Response.nextBoolean();
                                break;
                            case "UserIsTech":
                                UserIsTech = Response.nextBoolean();
                                break;
                            case "MachineNeedsTech":
                                MachineNeedsTech = Response.nextBoolean();
                                break;

                            default:
                                Response.skipValue();
                                break;
                        }
                    }  else {
                        Log.d(TAG, "Null field: " + Response.nextName());
                        Response.skipValue();
                    }
                }
                Response.close();
                connection.disconnect();

                if (UserIsTech) {
                    Log.d(TAG, "The user with Badge Number: " + badgeID +  " is a Tech");
                    return "UserIsTech";
                } else if ( UserHasCerts && MachineNeedsTech) {
                    Log.d(TAG,"The user with Badge Number: " + badgeID + " requires tech badge");
                    return "RequiresTech";
                } else if (UserHasCerts) {
                    Log.d(TAG, "The user with Badge Number: " + badgeID +  " was allowed");
                    return "UserIsAllowed";
                } else {
                    Log.d(TAG, "The user with Badge Number: " + badgeID + " was denied access");
                    return "UserIsDenied";
                }


            } catch (Exception e) {
                Log.d(TAG, "problem contacting API, printing stack trace:");
                e.printStackTrace();
                return "Exception";
            }


        }
    }

    static class TILTPostTechTask extends AsyncTask<String, Void, Boolean> {

        final String TAG = "TILTPOSTTech";
        Context context;
         interface OnSentEmailListener{
            void onSentEmail(DatabaseConnector.TILTPostTechTask TechEmail);
        }
         OnSentEmailListener onSentEmailListener;

        public void setOnSentEmailListener(OnSentEmailListener onSentEmailListener) {
            this.onSentEmailListener = onSentEmailListener;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (onSentEmailListener != null) {

                onSentEmailListener.onSentEmail(this);
            }
            super.onPostExecute(aBoolean);

        }

        public void setContext(Context ctx) {
            context = ctx;
        }

        @Override
        protected synchronized Boolean doInBackground(String... params) {

            String sessionID = "0";

            if (params.length >= 1) {
                sessionID = params[0];
            }

            final String content = "testContent";//content of the email message
            final String APIConnectionUrl = "http://" +
                    baseServerUrl +
                    "/TILTWebApi/api/technicians" +
                    "?sessionID=" + sessionID +
                    "&machineIP=" + machineID +
                    "&content=" + content;

            try {
                URL url = new URL(APIConnectionUrl);
                Log.d(TAG, url.toString());

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();


                final String TILT_API_KEY = "basic VElMVFdlYkFQSToxM1RJTFRXZWJBUEkxMw==";

                Log.d("TILTAPI", "Using key: " + TILT_API_KEY);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", TILT_API_KEY);
                int ResponseCode = connection.getResponseCode();
                Log.d("TILTAPI", "Respose code: " + String.valueOf(ResponseCode));

                if (ResponseCode == 204) {
                    Log.d(TAG, "email sent successfully");
                    connection.disconnect();
                    return true;
                } else {
                    Log.d(TAG, "invalid response code for POSTTech method");
                    connection.disconnect();
                    return false;
                }

            } catch (Exception e) {
                Log.d(TAG, "problem contacting API, printing stack trace:");
                e.printStackTrace();
                return false;
            }
        }
    }

    static class TILTGetTechTask extends AsyncTask<Void, Void, Void> {

        final String TAG = "TILTGETTech";
        Context context;

         interface OnFinishedParsingListener{
            void onFinishedParsing();
        }
         OnFinishedParsingListener onFinishedParsingListener;

         void setOnFinishedParsingListener(OnFinishedParsingListener onFinishedParsingListener) {
            this.onFinishedParsingListener = onFinishedParsingListener;
        }

        @Override
        protected void onPostExecute(Void avoid) {
            if (onFinishedParsingListener != null) {
                onFinishedParsingListener.onFinishedParsing();
            }
            super.onPostExecute(avoid);

        }

        public void setContext(Context ctx) {
            context = ctx;
        }

        @Override
        protected synchronized Void doInBackground(Void... params) {

            final String APIConnectionUrl = "http://" +
                    baseServerUrl +
                    "/TILTWebApi/api/technicians";

            try {
                URL url = new URL(APIConnectionUrl);
                Log.d(TAG, url.toString());

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                JsonReader ResponseReader = TILTAPITask(connection, "GET");

                if (ResponseReader == null) {
                    Log.d(TAG, "JSON response was null");
                    return null;
                }

                LabTechList.clear();
                ResponseReader.beginArray();
                while (ResponseReader.hasNext()) {
                    LabTech temp = new LabTech();
                    boolean isActive = false;
                    Log.d(TAG, "Received tech, extracting details.");


                    ResponseReader.beginObject();
                    while (ResponseReader.hasNext()) {
                        if (ResponseReader.peek() != JsonToken.NULL && ResponseReader.peek() != null) {
                            String key = ResponseReader.nextName();
                            switch (key) {
                                case ("LabTechID"):
                                    temp.LabTechID = ResponseReader.nextInt();
                                    Log.d(TAG, key + ": " + String.valueOf(temp.LabTechID));
                                    break;
                                case ("FirstName"):
                                    temp.firstName = ResponseReader.nextString();
                                    Log.d(TAG, key + ": " + temp.firstName);
                                    break;
                                case ("LastName"):
                                    temp.lastName = ResponseReader.nextString();
                                    Log.d(TAG, key + ": " + temp.lastName);
                                    break;
                                case ("Email"):
                                    temp.email = ResponseReader.nextString();
                                    Log.d(TAG, key + ": " + temp.email);
                                    break;
                                case ("PhoneNumber"):
                                    temp.phoneNumber = ResponseReader.nextString();
                                    Log.d(TAG, key + ": " + temp.phoneNumber);
                                    break;
                                case "Photo":
                                    temp.Image = ImageParser(ResponseReader.nextString());
                                    break;
                                case "Active":
                                    isActive = ResponseReader.nextBoolean();
                                    break;

                                default:
                                    ResponseReader.skipValue();
                                    break;
                            }
                        }  else {
                            Log.d(TAG, "Null field: " + ResponseReader.nextName());
                            ResponseReader.skipValue();
                        }
                    }
                    if (isActive) {
                        LabTechList.add(temp);
                    }
                    ResponseReader.endObject();

                }
                ResponseReader.endArray();
                ResponseReader.close();
                Log.d(TAG, "Received information for " + String.valueOf(LabTechList.size()) + " techs");
            } catch (Exception e) {
                Log.d(TAG, "problem contacting API, printing stack trace:");
                e.printStackTrace();
            }

            return null;
        }
    }

    static synchronized JsonReader PPEJsonParse(JsonReader Response) throws IOException {
        Response.beginArray();

        PPEList.clear();
        while (Response.hasNext()) {
            PPE ppe = new PPE();
            Response.beginObject();
            Log.d("TILTPOSTUser", "found PPE, extracting details");

            while (Response.hasNext()) {
                //parse response for PPE info
                //if the response is not empty, set UserAuthorized to true
                String key = Response.nextName();
                if (Response.peek() != JsonToken.NULL) {
                    switch (key) {
                        case "PPEID":
                            ppe.PPEID = Response.nextInt();
                            Log.d("TILTPOSTUser", key + ": " + String.valueOf(ppe.PPEID));
                            break;
                        case "PPE":
                            ppe.name = Response.nextString();
                            Log.d("TILTPOSTUser", key + ": " + ppe.name);
                            break;
                        case "Image":
                            ppe.Image = ImageParser(Response.nextString());
                            break;
                        case "Required":
                            ppe.Required = Response.nextBoolean();
                            Log.d("TILTPOSTUser", key + ": " + String.valueOf(ppe.Required));
                            break;
                        case "Restricted":
                            ppe.Restricted = Response.nextBoolean();
                            Log.d("TILTPOSTUser", key + ": " + String.valueOf(ppe.Restricted));
                            break;

                        default:
                            Response.skipValue();
                            break;
                    }
                } else {
                    Log.d("TILTPOSTUser", "Null field: " + Response.nextName());
                    Response.skipValue();
                }
            }
            PPEList.add(ppe);
            Response.endObject();
        }
        Response.endArray();
        return Response;
    }


}