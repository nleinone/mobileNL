package com.example.mc2020lab;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.WorkManager;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulerActivity extends Utils {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GeofencingClient geofencingClient;

    public void openCalendar(TextView tvDate, String index, final String reminderDesc, final String reminderTime)
    {

        final TextView tvDateFinal = tvDate;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        final String indexFinal = index;
        //REF: https://www.codingdemos.com/android-datepicker-button/
        DatePickerDialog datePickerDialog = new DatePickerDialog(SchedulerActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                        tvDateFinal.setText(day + "/" + month + 1 + "/" + year);
                        String stringDate = tvDateFinal.getText().toString();
                        SharedPreferences pref_date = getApplicationContext().getSharedPreferences("pref_date", 0); // 0 - for private mode
                        pref_date.edit().putString("Date", stringDate).apply();
                        //Delete reminder and add new:
                        Context context = getApplicationContext();
                        WorkManager.getInstance(context).cancelAllWorkByTag(workTag+indexFinal);
                        reScheduleWorker(reminderDesc, stringDate, reminderTime, indexFinal);


                    }
                }, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    public void openClock(TextView tvTitleTime, String index, final String reminderDesc, final String stringDate)
    {
        //REF: https://stackoverflow.com/questions/17901946/timepicker-dialog-from-clicking-edittext
        final TextView tvTitleTimeFinal = tvTitleTime;

        // TODO Auto-generated method stub
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        final String indexFinal = index;
        mTimePicker = new TimePickerDialog(SchedulerActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                String stringTime = selectedHour + ":" + selectedMinute;
                tvTitleTimeFinal.setText(stringTime);
                SharedPreferences pref_time = getApplicationContext().getSharedPreferences("pref_time", 0); // 0 - for private mode
                pref_time.edit().putString("Time", stringTime).apply();

                //Delete reminder and add new:
                Context context = getApplicationContext();
                WorkManager.getInstance(context).cancelAllWorkByTag(workTag+indexFinal);
                reScheduleWorker(reminderDesc, stringDate, stringTime, indexFinal);

            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void setTextToRow(String description, String date, String time, String placeName, TableRow row, TableLayout table, String index)
    {

        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TextView descTxt=new TextView(this);
        descTxt.setText(description);
        descTxt.setPadding(60, 5, 5, 5);

        TextView tvDate = new TextView(this);
        tvDate.setText(date);
        tvDate.setPadding(5, 5, 5, 5);

        TextView tvTime = new TextView(this);
        tvTime.setText(time);
        tvTime.setPadding(5, 5, 5, 5);

        TextView tvLocation = new TextView(this);
        tvLocation.setText(placeName);
        tvLocation.setPadding(5, 5, 5, 5);

        SharedPreferences login_name_pref = getApplicationContext().getSharedPreferences("login_name", 0); // 0 - for private mode
        final String login_name = login_name_pref.getString("loginName", "NoName");

        Button deleteButton = new Button(this);
        final String index_final = index;
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                SharedPreferences pref = getApplicationContext().getSharedPreferences("reminder_info_preference", 0); // 0 - for private mode
                pref.edit().remove(login_name + "_" + "reminder" + index_final).apply();
                //Work tag: notificationWork + id
                String workTag = "notificationWork";
                Context context = getApplication();
                Log.v("SchedulerActivity", "Deleted reminder: " + (workTag + index_final));
                WorkManager.getInstance(context).cancelAllWorkByTag(workTag + index_final);
                checkAndLoadReminders();

            }
        });

        Button editButton = new Button(this);
        final String index_final2 = index;
        editButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                //Create input box:
                //Ref: https://stackoverflow.com/questions/18799216/how-to-make-a-edittext-box-in-a-dialog
                SharedPreferences pref = getApplicationContext().getSharedPreferences("reminder_info_preference", 0); // 0 - for private mode
                //Get current values for default
                String current_remainderInfo = pref.getString(login_name + "_" + "reminder" + index_final, "None");

                //From json string to hashmap:
                java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
                }.getType();
                Gson gson = new Gson();
                HashMap<String, String> reminder_information = gson.fromJson(current_remainderInfo, type);

                //Create alert box:
                AlertDialog.Builder alert = new AlertDialog.Builder(SchedulerActivity.this);

                //Create viewGroup for multiple views in the alert box:

                //ADD LAYOUTS FOR EVERY INFO:
                Context context = SchedulerActivity.this;
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                //Create Description editText and TextView to alert box:
                final String reminderDesc = reminder_information.get("Description");
                final EditText editTextDesct = new EditText(SchedulerActivity.this);
                editTextDesct.setText(reminderDesc);
                final TextView txDesc = new TextView((SchedulerActivity.this));
                txDesc.setText(getString(R.string.descriptionTx));
                layout.addView(txDesc);
                layout.addView(editTextDesct);

                //Set calendar button to edit box:
                final TextView tvTitleDate = new TextView(SchedulerActivity.this);
                tvTitleDate.setText(getString(R.string.dateTx));
                layout.addView(tvTitleDate);

                final String dateString = reminder_information.get("Date");

                final TextView tvDate = new TextView(SchedulerActivity.this);
                tvDate.setText(dateString);
                layout.addView(tvDate);

                //Button
                Button dateBtn = new Button(SchedulerActivity.this);
                dateBtn.setText(getString(R.string.changeDateTx));

                final String reminderTime = reminder_information.get("Time");

                dateBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openCalendar(tvDate, index_final2, reminderDesc, reminderTime);
                    }
                });
                layout.addView(dateBtn);

                //Add time edit:
                final TextView tvTitleTime = new TextView(SchedulerActivity.this);
                tvTitleDate.setText(getString(R.string.timeTx));
                layout.addView(tvTitleTime);

                final TextView tvTime = new TextView(SchedulerActivity.this);
                tvTime.setText(reminderTime);
                layout.addView(tvTime);

                //Button
                Button timeBtn = new Button(SchedulerActivity.this);
                timeBtn.setText(getString(R.string.ChangeTimeTx));

                timeBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openClock(tvTime, index_final2, reminderDesc, dateString );
                    }
                });
                layout.addView(timeBtn);

                //Set texts to alert box
                alert.setMessage("Edit reminder values");
                alert.setTitle("Edit");

                alert.setView(layout);

                //Create positive button
                alert.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Get string:
                        String stringDescription = editTextDesct.getText().toString();
                        String stringDate = tvDate.getText().toString();
                        String stringTime = tvTime.getText().toString();

                        Map<String, String> reminderInfo = new HashMap<>();

                        reminderInfo = changeReminderValue(stringDescription, "Description", reminderInfo);
                        reminderInfo = changeReminderValue(stringDate, "Date", reminderInfo);
                        reminderInfo = changeReminderValue(stringTime, "Time", reminderInfo);

                        SharedPreferences pref = getApplicationContext().getSharedPreferences("reminder_info_preference", 0); // 0 - for private mode

                        Gson gson = new Gson();
                        String reminderInfoJSON = gson.toJson(reminderInfo);
                        pref.edit().putString(login_name + "_" + "reminder" + index_final, reminderInfoJSON).apply();
                        checkAndLoadReminders();
                    }
                });

                //Create negative button
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                alert.show();
            }
        });

        //Delete button for reminder
        deleteButton.setText(getString(R.string.DeleteTxBtn));
        editButton.setText(getString(R.string.EditTxBtn));

        row.addView(descTxt);
        row.addView(tvDate);
        row.addView(tvTime);
        row.addView(tvLocation);
        row.addView(editButton);
        row.addView(deleteButton);
        table.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    public void checkAndLoadReminders()
    {
        Gson gson = new Gson();

        TableLayout table = findViewById(R.id.tableLayout);
        table.removeAllViews();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("reminder_info_preference", 0); // 0 - for private mode
        SharedPreferences login_name_pref = getApplicationContext().getSharedPreferences("login_name", 0); // 0 - for private mode
        String login_name = login_name_pref.getString("loginName", "NoName");

        for (int i = 1; i < 12; i++) {
            String index = Integer.toString(i);

            if (pref.contains(login_name + "_" + "reminder" + index)) {

                String storedHashMapString = pref.getString(login_name + "_" + "reminder" + index, "oopsDintWork");
                java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
                }.getType();

                HashMap<String, String> reminder_information = gson.fromJson(storedHashMapString, type);

                //Load reminder information

                String longitude = reminder_information.get("Longitude");
                String latitude = reminder_information.get("Latitude");
                String index_counter = reminder_information.get("Index");
                String description = reminder_information.get("Description");
                String date = reminder_information.get("Date");
                String time = reminder_information.get("Time");
                String placeName = reminder_information.get("Location");
                String delay = reminder_information.get("Delay");

                //LOGGED REMINDER INFORMATION:
                Log.d("REMINDERTAG", "Reminder: " + index_counter);
                Log.d("REMINDERTAG", "Description: " + description);
                Log.d("REMINDERTAG", "Date: " + date);
                Log.d("REMINDERTAG", "Time: " + time);
                Log.d("REMINDERTAG", "Location: " + placeName);
                Log.d("REMINDERTAG", "Longitude: " + longitude);
                Log.d("REMINDERTAG", "Latitude: " + latitude);

                //Location info format:
                // "reminder" + "_" + stringLocationIndex + "_" + loginNameFinal +
                //    "_" + stringDescription + "_" + stringLongitude + "_" + stringLatitude
                //https://stackoverflow.com/questions/11342975/android-create-textviews-in-tablerows-programmatically

                TableRow row1 = new TableRow(this);
                setTextToRow(description, date, time, placeName, row1, table, index);

                //Create geofences according to the reminder locations:

                createGeoFences(index, latitude, longitude, delay);
            }

            else
            {
                Log.v("Scheduler" ,"Work canceled: " + workTag + index);
                WorkManager.getInstance(this).cancelAllWorkByTag(workTag + index);
                //Remove geofences:
                List idList = new ArrayList();
                idList.add(index);
                geofencingClient = LocationServices.getGeofencingClient(this);
                geofencingClient.removeGeofences(idList);

            }
        }
    }

    //If location permission is not enabled, ask it
    private void enableLocationIfGranted() {
        //If fine location access is NOT "PERMISSION GRANTED"...
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // SHOW MESSAGE FOR DENYING THE PERMISSION!
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                finish();
            }
            else{
                //...Request permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);

        //Check location permission
        enableLocationIfGranted();
        checkAndLoadReminders();
    }

    protected void onResume() {
        super.onResume();
        checkAndLoadReminders();
    }

    public void onClick(View v) {

        if (v.getId() == R.id.goToReminderBtn) {
            startActivity(new Intent(SchedulerActivity.this, AddReminderActivity.class));
        }
        if (v.getId() == R.id.checkMapBtn) {
            //REF: https://stackoverflow.com/questions/25147612/can-i-check-which-the-previous-activity-was-android
            Intent mIntent = new Intent(this, MapsActivity.class); //'this' is Activity A
            mIntent.putExtra("FROM_ACTIVITY", "SchedulerActivity");
            startActivity(mIntent);
        }
    }
}