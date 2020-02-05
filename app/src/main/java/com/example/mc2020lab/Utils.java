package com.example.mc2020lab;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class Utils extends AppCompatActivity {

    /*
    * This class contains common useful functions for other Activity classes.
    * */

    private GeofencingClient geofencingClient;
    public static final String workTag = "notificationWork";

    public int calculateDelay(String date, String time)
    {
        //get current time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        String[] currentDateAndTimeSplit = currentDateandTime.split("_");

        int currentYear  = Integer.parseInt(currentDateAndTimeSplit[0]);
        int currentMonth = Integer.parseInt(currentDateAndTimeSplit[1]);
        int currentDay   = Integer.parseInt(currentDateAndTimeSplit[2]);
        int currentHour = Integer.parseInt(currentDateAndTimeSplit[3]);
        int currentMin  = Integer.parseInt(currentDateAndTimeSplit[4]);

        Log.v("Delay YY: ", currentDateAndTimeSplit[0]);
        Log.v("Delay Month: ", currentDateAndTimeSplit[1]);
        Log.v("Delay DD: ", currentDateAndTimeSplit[2]);
        Log.v("Delay HH: ", currentDateAndTimeSplit[3]);
        Log.v("Delay MM: ", currentDateAndTimeSplit[4]);

        //Init values:
        int delayFromDays = 0;
        int delayFromHours = 0;
        int delayFromMins = 0;

        if(!date.equals("No date"))
        {
            String[] dateSplit = date.split("/");
            int inputDay = Integer.parseInt(dateSplit[0]);
            int inputMonth = Integer.parseInt(dateSplit[1]);
            int inputYear = Integer.parseInt(dateSplit[2]);

            Log.v("Delay DD Input: ", dateSplit[0]);
            Log.v("Delay Month Input: ", dateSplit[1]);
            Log.v("Delay YY Input: ", dateSplit[2]);

            int diffDay   = inputDay - currentDay;
            Log.v("Delay Day Diff: ", Integer.toString(diffDay));
            //int delayFromYears = 0; //Next year reminders? With workManager, only delay can be set, this would mean setting up a 31 536 000 000‬ ms delay...
            delayFromDays = diffDay * 86400000; //One day delays the reminder for 86400000 ms.
        }

        if(!time.equals("No time"))
        {
            String[] timeSplit = time.split(":");
            int inputHour = Integer.parseInt(timeSplit[0]);
            int inputMin = Integer.parseInt(timeSplit[1]);

            Log.v("Delay Hour Input: ", timeSplit[0]);
            Log.v("Delay Min Input: ", timeSplit[0]);

            int diffHour  = inputHour - currentHour;
            int diffMin   = inputMin - currentMin;
            Log.v("Delay Hour Diff: ", Integer.toString(diffHour));
            Log.v("Delay Min Diff: ", Integer.toString(diffMin));
            delayFromHours = diffHour * 3600000;
            delayFromMins = diffMin * 60000;

        }
        //int diffYear  = currentYear - inputYear;

        int delay = delayFromDays + delayFromHours + delayFromMins;

        Log.v("Delay: ", Integer.toString(delay));

        return delay;
    }

    public void buildGeoFence(double LATITUDE, double LONGITUDE, String GEOFENCE_REQ_ID, String stringDURATION)
    {
        long DURATION = 0;
        if(stringDURATION != null)
        {
            DURATION = Long.parseLong(stringDURATION);
        }

        float RADIUS = 10;
        Geofence geofence = new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID) // Geofence ID
                .setCircularRegion( LATITUDE, LONGITUDE, RADIUS) // defining fence region
                .setExpirationDuration( DURATION ) // expiring date
                // Transition types that it should look for
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();

        List geoFenceList = new ArrayList();
        geoFenceList.add(geofence);

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geoFenceList);
        builder.build();
    }

    public void createGeoFences(String GEOFENCE_REQ_ID, String stringLATITUDE, String stringLONGITUDE, String stringDURATION)
    {
        //REF: https://developer.android.com/training/location/geofencing
        //https://code.tutsplus.com/tutorials/how-to-work-with-geofences-on-android--cms-26639

        geofencingClient = LocationServices.getGeofencingClient(this);

        Log.v("Utils: ", "t1");
        Log.v("Utils: ", "stringLatitude: " + stringDURATION);
        try
        {
            double LATITUDE = Double.parseDouble(stringLATITUDE);
            double LONGITUDE = Double.parseDouble(stringLONGITUDE);
            buildGeoFence(LATITUDE, LONGITUDE, GEOFENCE_REQ_ID, stringDURATION);
        }
        catch(NullPointerException e)
        {
            double LATITUDE = 0.0;
            double LONGITUDE = 0.0;
            buildGeoFence(LATITUDE, LONGITUDE, GEOFENCE_REQ_ID, stringDURATION);
        }
    }

    public void reScheduleWorker(String reminderDesc, String stringDate, String stringTime, String stringId)
    {
        SharedPreferences workerData = getApplicationContext().getSharedPreferences("WorkerData", 0);
        workerData.edit().putString("TaskDesc", reminderDesc).apply();
        workerData.edit().putString("TaskDate", stringDate).apply();
        workerData.edit().putString("TaskTime", stringTime).apply();
        workerData.edit().putString("TaskId", stringId).apply();

        int delay = calculateDelay(stringDate, stringTime);

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(UploadWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(workTag + stringId)
                .build();

        WorkManager.getInstance(Utils.this).enqueue(notificationWork);
    }

    public Map<String, String> changeReminderValue(String textInput, String reminderString, Map<String, String> reminderInfo)
    {

        if (!(textInput.equals("")))
        {
            reminderInfo.put(reminderString, textInput);
        }
        return reminderInfo;
    }





}
