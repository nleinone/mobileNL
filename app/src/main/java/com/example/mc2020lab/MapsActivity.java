package com.example.mc2020lab;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
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
        if (Build.VERSION.SDK_INT >= 23)
        {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        }
        else
        {
             requestLocation();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
        LatLng current_location = new LatLng(doubleLatitude, doubleLongitude);
        mMap.addMarker(new MarkerOptions().position(current_location).title("Current position marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));

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

    }

    //REF: https://www.thecodecity.com/2017/03/location-tracker-android-app-complete.html
    public void requestLocation()
    {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = locationManager.getBestProvider(criteria, true);
        //locationManager.requestLocationUpdates(provider, 10000, 10, true);
    }

    @Override
    public void onLocationChanged(Location location) {

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
