//REF:https://github.com/nleinone/CrowdSourcer/blob/master/app/src/main/java/com/example/nikol/growdsourcer/LocationTracker.java
package com.example.mc2020lab;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import android.content.SharedPreferences;
import android.os.IBinder;
import android.content.Intent;
import android.Manifest;
import android.location.Location;
import android.content.pm.PackageManager;
import android.app.Service;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class LocationTracker extends Service {

    private static final String TAG = LocationTracker.class.getSimpleName();
    private String UserID;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        System.out.println("TESTI13");
        //Check permission
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            System.out.println("TESTI12");
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    System.out.println("TESTI14");
                    //Get UserID:
                    //Get a reference to the database, so your app can perform read and write operations//
                    //DatabaseReference ref = mFirebaseDatabase.getInstance().getReference().child(UserID).child("Location");
                    Location location = locationResult.getLastLocation();
                    System.out.println("TESTI: " + location);
                    if (location != null) {
                        System.out.println("TESTI10");
                        //Save the location data to the database//
                        //For Extra information:
                        //myRef.child(UserID).child("Location Info").setValue(location);

                        //For only coordinates:
                        double Longitude = location.getLongitude();
                        double Latitude = location.getLatitude();

                        String stringLongitude = Double.toString(Longitude);
                        String stringLatitude = Double.toString(Latitude);

                        Log.v("MAPS", "longitude");
                        Log.v("MAPS", "latitude");

                        SharedPreferences login_name_pref = getApplicationContext().getSharedPreferences("login_name", 0); // 0 - for private mode
                        String loginName = login_name_pref.getString("loginName", "No name");

                        SharedPreferences pref = getApplicationContext().getSharedPreferences("shared_preference", 0); // 0 - for private mode

                        pref.edit().putString(loginName + "_" + "longitude", stringLongitude).apply();
                        pref.edit().putString(loginName + "_" + "latitude", stringLatitude).apply();

                        Log.v("Tracker", loginName);
                        Log.v("Tracker", stringLatitude);
                        Log.v("Tracker", stringLongitude);


                    }
                }
            }, null);
        }
    }
}