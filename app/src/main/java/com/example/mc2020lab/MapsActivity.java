package com.example.mc2020lab;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    public void getOwnCoordinates(int permission)
    {
        LocationRequest request = new LocationRequest();
        request.setInterval(1000);

        if (permission == PackageManager.PERMISSION_GRANTED) {

            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            //Save own coordinates to pref:
                            double Longitude = location.getLongitude();
                            double Latitude = location.getLatitude();

                            String stringLongitude = Double.toString(Longitude);
                            String stringLatitude = Double.toString(Latitude);

                            SharedPreferences login_name_pref = getApplicationContext().getSharedPreferences("login_name", 0); // 0 - for private mode
                            String loginName = login_name_pref.getString("LoginName", "No name");

                            SharedPreferences pref = getApplicationContext().getSharedPreferences("shared_preference", 0); // 0 - for private mode

                            pref.edit().putString(loginName + "_" + "longitude", stringLongitude).apply();
                            pref.edit().putString(loginName + "_" + "latitude", stringLatitude).apply();
                        }
                    }
            }, null);
        }
    }

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

        //Check permission
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        //Get current location
        getOwnCoordinates(permission);

        SharedPreferences login_name_pref = getApplicationContext().getSharedPreferences("login_name", 0); // 0 - for private mode
        String loginName = login_name_pref.getString("LoginName", "No name");

        SharedPreferences pref = getApplicationContext().getSharedPreferences("shared_preference", 0); // 0 - for private mode
        String longitude = pref.getString(loginName + "_" + "longitude", "No longitude");
        String latitude = pref.getString(loginName + "_" + "latitude", "No latitude");

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
}
