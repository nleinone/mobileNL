package com.example.mc2020lab;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.GoogleMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class SchedulerActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    public Map<String, String> changeReminderValue(String textInput, String reminderString, Map<String, String> reminderInfo)
    {

        if (!(textInput.equals("")))
        {
            reminderInfo.put(reminderString, textInput);
        }
        return reminderInfo;
    }

    public void openCalendar(TextView tvDate)
    {

        final TextView tvDateFinal = tvDate;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        //REF: https://www.codingdemos.com/android-datepicker-button/
        DatePickerDialog datePickerDialog = new DatePickerDialog(SchedulerActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                        tvDateFinal.setText(day + "/" + month + 1 + "/" + year);
                        String stringDate = tvDateFinal.getText().toString();
                        SharedPreferences pref_date = getApplicationContext().getSharedPreferences("pref_date", 0); // 0 - for private mode
                        pref_date.edit().putString("Date", stringDate).apply();

                    }
                }, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    public void openClock(TextView tvTitleTime)
    {
        //REF: https://stackoverflow.com/questions/17901946/timepicker-dialog-from-clicking-edittext
        final TextView tvTitleTimeFinal = tvTitleTime;

        // TODO Auto-generated method stub
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(SchedulerActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                String stringTime = selectedHour + ":" + selectedMinute;
                tvTitleTimeFinal.setText(stringTime);
                SharedPreferences pref_time = getApplicationContext().getSharedPreferences("pref_time", 0); // 0 - for private mode
                pref_time.edit().putString("Time", stringTime).apply();

            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void setTextToRow(String description, String date, String time, TableRow row, TableLayout table, String index)
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

        SharedPreferences login_name_pref = getApplicationContext().getSharedPreferences("login_name", 0); // 0 - for private mode
        final String login_name = login_name_pref.getString("loginName", "NoName");

        Button deleteButton = new Button(this);
        final String index_final = index;
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                SharedPreferences pref = getApplicationContext().getSharedPreferences("reminder_info_preference", 0); // 0 - for private mode
                pref.edit().remove(login_name + "_" + "reminder" + index_final).apply();
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
                final EditText editTextDesct = new EditText(SchedulerActivity.this);
                editTextDesct.setText(reminder_information.get("Description"));
                final TextView txDesc = new TextView((SchedulerActivity.this));
                txDesc.setText("Description: ");
                layout.addView(txDesc);
                layout.addView(editTextDesct);

                //Set calendar button to edit box:
                //Text
                final TextView tvTitleDate = new TextView(SchedulerActivity.this);
                tvTitleDate.setText("Date:");
                layout.addView(tvTitleDate);

                final TextView tvDate = new TextView(SchedulerActivity.this);
                tvDate.setText(reminder_information.get("Date"));
                layout.addView(tvDate);
                //Button
                Button dateBtn = new Button(SchedulerActivity.this);
                dateBtn.setText("Change date");

                dateBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openCalendar(tvDate);
                    }
                });
                layout.addView(dateBtn);

                //Add time edit:
                //Text
                final TextView tvTitleTime = new TextView(SchedulerActivity.this);
                tvTitleDate.setText("Time:");
                layout.addView(tvTitleTime);

                final TextView tvTime = new TextView(SchedulerActivity.this);
                tvTime.setText(reminder_information.get("Time"));
                layout.addView(tvTime);
                //Button
                Button timeBtn = new Button(SchedulerActivity.this);
                timeBtn.setText("Change time");

                timeBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openClock(tvTime);
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

                        Map<String, String> reminderInfo = new HashMap<String, String>();

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
        deleteButton.setText("Delete");
        editButton.setText("Edit");

        row.addView(descTxt);
        row.addView(tvDate);
        row.addView(tvTime);
        row.addView(editButton);
        row.addView(deleteButton);
        table.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    public void checkAndLoadReminders()
    {
        Gson gson = new Gson();

        TableLayout table = (TableLayout) findViewById(R.id.tableLayout);
        table.removeAllViews();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("reminder_info_preference", 0); // 0 - for private mode
        SharedPreferences login_name_pref = getApplicationContext().getSharedPreferences("login_name", 0); // 0 - for private mode
        String login_name = login_name_pref.getString("loginName", "NoName");

        for (int i = 1; i < 12; i++) {
            String str = Integer.toString(i);

            if (pref.contains(login_name + "_" + "reminder" + str)) {

                String storedHashMapString = pref.getString(login_name + "_" + "reminder" + str, "oopsDintWork");
                java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
                }.getType();

                HashMap<String, String> reminder_information = gson.fromJson(storedHashMapString, type);

                //Load reminder information
                String description = reminder_information.get("Description");
                String date = reminder_information.get("Date");
                String time = reminder_information.get("Time");
                //Location info format:
                // "reminder" + "_" + stringLocationIndex + "_" + loginNameFinal +
                //    "_" + stringDescription + "_" + stringLongitude + "_" + stringLatitude



                SharedPreferences pref_counter = getApplicationContext().getSharedPreferences("reminder_counter", 0); // 0 - for private mode
                String index = Integer.toString(i);

                //https://stackoverflow.com/questions/11342975/android-create-textviews-in-tablerows-programmatically

                TableRow row1 = new TableRow(this);

                setTextToRow(description, date, time, row1, table, index);
                //setTextToRow(days, table, row1);
                //setTextToRow(months, table, row1);
                //setTextToRow(hours, table, row1);
                //setTextToRow(mins, table, row1);
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

        else if (v.getId() == R.id.checkMapBtn) {
            startActivity(new Intent(SchedulerActivity.this, MapsActivity.class));

        }
    }
}
