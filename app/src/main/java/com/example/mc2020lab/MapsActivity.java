package com.example.mc2020lab;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION};
    MarkerOptions mo;
    Marker marker;
    LocationManager locationManager;

    //REF: https://mobikul.com/picking-location-with-map-pin-using-google-maps-in-android/
    //REF: https://abhiandroid.com/programming/googlemaps
    //AIzaSyCLF1Na9WALrQOZ99j7OLzx3ubG0kwnpRM
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mo = new MarkerOptions().position(new LatLng(0,0)).title("My current location");

        //Add permission test if failing
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted())
        {
            Log.v("onCreateMap", "t1");

            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        }
        else
        {
            Log.v("onCreateMap", "t2");
             requestLocation();
        }
        if(!isLocationEnabled())
        {
            Log.v("onCreateMap", "t3");
            showAlert(1);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        marker = mMap.addMarker(mo);

        SharedPreferences login_name_pref = getApplicationContext().getSharedPreferences("login_name", 0); // 0 - for private mode
        String loginName = login_name_pref.getString("loginName", "No name");

        SharedPreferences pref = getApplicationContext().getSharedPreferences("shared_preference", 0); // 0 - for private mode
        String longitude = pref.getString(loginName + "_" + "longitude", "No longitude");
        String latitude = pref.getString(loginName + "_" + "latitude", "No latitude");

        Log.v("Name", loginName);
        Log.v("Longitude", longitude);
        Log.v("Latitude", longitude);


        Double doubleLongitude = Double.parseDouble(longitude);
        Double doubleLatitude = Double.parseDouble(latitude);

        //Move camera to current location
        //REF: https://github.com/nleinone/CrowdSourcer/blob/master/app/src/main/java/com/example/nikol/growdsourcer/LocationTracker.java
        //LatLng current_location = new LatLng(doubleLatitude, doubleLongitude);
        //mMap.addMarker(new MarkerOptions().position(current_location).title("Current position marker"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));

        /*
        //Marker drag:
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng latLng = marker.getPosition();
                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                try {
                    android.location.Address address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        */
    }


    //REF: https://www.thecodecity.com/2017/03/location-tracker-android-app-complete.html
    public void requestLocation()
    {
        Log.v("onCreateMap", "t2.1");
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = locationManager.getBestProvider(criteria, true);
        if(Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(provider, 10000, 10, this);
            }
        }
    }

    public boolean isLocationEnabled()
    {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public boolean isPermissionGranted()
    {
        if(Build.VERSION.SDK_INT >= 23)
        {
            if(checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
            {
                Log.v("MAPS", "test");
                return true;
            }
            else
            {
                Log.v("MAPS", "test2");
                return false;
            }
        }
        else
        {
            Log.v("MAPS", "Wrong API (Must be more than 23");
            return false;
        }
    }

    public void showAlert(final int status)
    {
        String msg;
        String title;
        String btnTxt;

        DialogInterface.OnClickListener buttonListener_positive =
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(status == 1)
                        {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        }
                        else
                        {
                            if(Build.VERSION.SDK_INT >= 23)
                            {
                                requestPermissions(PERMISSIONS, PERMISSION_ALL);
                            }
                        }
                    }
                };
        DialogInterface.OnClickListener buttonListener_negative =
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };
        if (status == 1)
        {
            msg = "Location is off!";
            title = "Enable location!";
            btnTxt = "Location Settings";
        }
        else
        {
            msg = "Please allow this app to use location!";
            title = "Permission access";
            btnTxt = "Grant";
        }

        AlertDialog introDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(btnTxt, buttonListener_positive)
                .setNegativeButton("Cancel", buttonListener_negative)
                .setCancelable(false)
                .create();
        introDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {

        LatLng myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        String stringCoordinates = myCoordinates.toString();
        Log.v("Current Location:", stringCoordinates);
        marker.setPosition(myCoordinates);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myCoordinates));

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
