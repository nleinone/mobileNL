package com.example.mc2020lab;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    int location_index = 0;
    boolean firstLoad = true;
    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION};
    MarkerOptions mo;
    Marker marker;
    LocationManager locationManager;

    //REF: https://mobikul.com/picking-location-with-map-pin-using-google-maps-in-android/
    //REF: https://abhiandroid.com/programming/googlemaps
    //AIzaSyCLF1Na9WALrQOZ99j7OLzx3ubG0kwnpRM
    public Map<String, String> changeReminderValue(String textInput, String reminderString, Map<String, String> reminderInfo)
    {

        if (!(textInput.equals("")))
        {
            reminderInfo.put(reminderString, textInput);
        }
        return reminderInfo;
    }

    public void openPlaceNameWindow(String loginName, String longitude, String latitude)
    {
        AlertDialog.Builder placeNameWindow = new AlertDialog.Builder(MapsActivity.this);

        Log.d("DEBUG","ow1");

        final String loginNameFinal = loginName;
        final String stringLongitude = longitude;
        final String stringLatitude = latitude;
        //Create viewGroup for multiple views in the alert box:

        //ADD LAYOUTS FOR EVERY INFO:
        Context context = MapsActivity.this;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        //Create Description editText and TextView to alert box:
        final EditText editTextPlaceName = new EditText(MapsActivity.this);
        final TextView txPlaceNameTitle = new TextView((MapsActivity.this));
        txPlaceNameTitle.setText("Place name: ");

        layout.addView(txPlaceNameTitle);
        layout.addView(editTextPlaceName);



        //Set texts to alert box
        placeNameWindow.setMessage("Choose location for the reminder");
        placeNameWindow.setTitle("Choose location");

        placeNameWindow.setView(layout);

        Log.d("DEBUG","ow2");

        placeNameWindow.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                Log.d("DEBUG","ow2");
                Map<String, String> reminderInfo = new HashMap<String, String>();
                //Get string:
                String stringDescription = editTextPlaceName.getText().toString();

                //setContentView(R.layout.activity_maps);

                reminderInfo = changeReminderValue(stringDescription, "Location", reminderInfo);

                SharedPreferences location = getApplicationContext().getSharedPreferences("Location", 0); // 0 - for private mode

                int location_index_increased = location_index + 1;
                String stringLocationIndex = Integer.toString(location_index_increased);

                Gson gson = new Gson();
                String reminderInfoJSON = gson.toJson(reminderInfo);
                location.edit().putString("Location_package", "reminder" + "_" + stringLocationIndex + "_" + loginNameFinal +
                        "_" + stringDescription + "_" + stringLongitude + "_" + stringLatitude).apply();

                finish();
            }
        });
        placeNameWindow.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d("DEBUG","ow3");

                dialog.dismiss();
            }
        });
        placeNameWindow.show();
        Log.d("DEBUG","ow4");



    }

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
        final String loginName = login_name_pref.getString("loginName", "No name");

        //String longitude = pref.getString(loginName + "_" + "longitude", "No longitude");
        //String latitude = pref.getString(loginName + "_" + "latitude", "No latitude");

        //Touch functionalities:
        //REF: https://stackoverflow.com/questions/25153344/getting-coordinates-of-location-touch-on-google-map-in-android
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                Log.d("DEBUG","Map clicked [" + point.latitude + " / " + point.longitude + "]");
                //Do your stuff with LatLng here
                //Then pass LatLng to other activity

                String longitude = Double.toString(point.longitude);
                String latitude = Double.toString(point.latitude);

                openPlaceNameWindow(loginName, longitude, latitude);

            }
        });

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

        if(firstLoad = true)
        {
            float zoom = 16.0f;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myCoordinates, zoom));
            firstLoad = false;
        }
        else
        {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myCoordinates));
        }
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
