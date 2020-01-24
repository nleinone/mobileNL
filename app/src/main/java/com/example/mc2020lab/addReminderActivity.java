package com.example.mc2020lab;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import 	java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddReminderActivity extends AppCompatActivity {

    public static final String workTag = "notificationWork";

    public void increaseEventCounter(SharedPreferences pref_counter)
    {
        if(pref_counter.contains("Event_counter"))
        {
            //Get counter

            String counter = pref_counter.getString("Event_counter", "1");
            int counter_int = Integer.parseInt(counter);
            //Add number
            int increased_counter = counter_int + 1;

            String str_counter = Integer.toString(increased_counter);

            //Save Counter
            pref_counter.edit().putString("Event_counter", str_counter).apply();
        }

        else
        {
            pref_counter.edit().putString("Event_counter", "1").apply();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);
    }

    @Override
    public void onResume() {
        super.onResume();

        TextView tvLocation = findViewById(R.id.tvLocation);
        SharedPreferences pref_location = getApplicationContext().getSharedPreferences("Location", 0);
        String placeName = pref_location.getString("Location_package", "Press 'Open Map'");
        if(!placeName.equals("Press 'Open Map'"))
        {
            String[] location_info_list = placeName.split("_");
            placeName = location_info_list[3];

            Log.v("test", placeName);
        }

        tvLocation.setText(placeName);

    }

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

    public void scheduleWorker(String reminderDesc, String stringDate, String stringTime, String stringId)
    {
        SharedPreferences workerData = getApplicationContext().getSharedPreferences("WorkerData", 0);
        workerData.edit().putString("TaskDesc", reminderDesc).apply();
        workerData.edit().putString("TaskDate", stringDate).apply();
        workerData.edit().putString("TaskTime", stringTime).apply();
        workerData.edit().putString("TaskId", stringId).apply();

        Log.v("Worker reminderDesc: ", reminderDesc);
        Log.v("Worker TaskDate: ", stringDate);
        Log.v("Worker TaskTime: ", stringTime);
        Log.v("Worker TaskId: ", stringId);

        //Calculate delay from current date and time
        int delay = calculateDelay(stringDate, stringTime);
        //Get current time:


        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(UploadWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(workTag + stringId)
                .build();

        WorkManager.getInstance(AddReminderActivity.this).enqueue(notificationWork);

    }

    public void onClick(View v) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("reminder_info_preference", 0); // 0 - for private mode
        SharedPreferences pref_counter = getApplicationContext().getSharedPreferences("reminder_counter", 0); // 0 - for private mode
        SharedPreferences login_name_pref = getApplicationContext().getSharedPreferences("login_name", 0); // 0 - for private mode
        String login_name = login_name_pref.getString("loginName", "NoName");

        final Map<String, String> reminderInfo = new HashMap<String, String>();

        if (v.getId() == R.id.selectDateBtn)
        {

            //Fix the wrong date problem:
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            final TextView tvDate = findViewById(R.id.textViewDay);

            //REF: https://www.codingdemos.com/android-datepicker-button/
            DatePickerDialog datePickerDialog = new DatePickerDialog(AddReminderActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {


                            tvDate.setText(day + "/" + month + 1 + "/" + year);
                            String stringDate = tvDate.getText().toString();

                            SharedPreferences pref_date = getApplicationContext().getSharedPreferences("pref_date", 0); // 0 - for private mode

                            pref_date.edit().putString("Date", stringDate).apply();

                        }
                    }, year, month, dayOfMonth);
            datePickerDialog.show();
        }

        if (v.getId() == R.id.selectTimeBtn)
        {
            //REF: https://stackoverflow.com/questions/17901946/timepicker-dialog-from-clicking-edittext
            final TextView tvHours = findViewById(R.id.textViewHour);

            // TODO Auto-generated method stub
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(AddReminderActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    String stringTime = selectedHour + ":" + selectedMinute;
                    tvHours.setText(stringTime);
                    SharedPreferences pref_time = getApplicationContext().getSharedPreferences("pref_time", 0); // 0 - for private mode
                    pref_time.edit().putString("Time", stringTime).apply();


                }
            }, hour, minute, true);//Yes 24 hour time
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();

        }

        if (v.getId() == R.id.selectLocationBtn)
        {
            Log.v("Addrem", "t1");
            //REF: https://stackoverflow.com/questions/25147612/can-i-check-which-the-previous-activity-was-android
            Intent mIntent = new Intent(this, MapsActivity.class); //'this' is Activity A
            mIntent.putExtra("FROM_ACTIVITY", "AddReminderActivity");
            startActivity(mIntent);
        }

        if (v.getId() == R.id.clearDataBtn) {

            pref.edit().clear().apply();
            pref_counter.edit().clear().apply();
            finish();
        }

        if (v.getId() == R.id.addReminderBtn) {
            //Test if there is too many reminders
            Boolean canCreateReminder = true;
            for (int i = 1; i < 11; i++) {
                String str = Integer.toString(i);
                if (pref.contains("reminder" + str)) {

                    if (i == 10)
                    {
                        DialogInterface.OnClickListener buttonListener =
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                };

                        AlertDialog introDialog = new AlertDialog.Builder(this)
                                .setTitle("Too many reminders!")
                                .setMessage("Delete 1 reminder if you want to add more!")
                                .setPositiveButton("Ok", buttonListener)
                                .setCancelable(false)
                                .create();
                        introDialog.show();
                        canCreateReminder = false;
                    }
                    else {
                        Log.v("", "ok");
                    }
                }
            }

            Gson gson = new Gson();

            increaseEventCounter(pref_counter);

            EditText EditTextDescription = findViewById(R.id.editTextDesc);
            SharedPreferences pref_date = getApplicationContext().getSharedPreferences("pref_date", 0); // 0 - for private mode
            SharedPreferences pref_time = getApplicationContext().getSharedPreferences("pref_time", 0); // 0 - for private mode
            SharedPreferences pref_location = getApplicationContext().getSharedPreferences("Location", 0);

            //Get location package, unpack it
            String location_package = pref_location.getString("Location_package", "no location");
            String[] location_info_list = location_package.split("_");
            //"reminder" + "_" + stringLocationIndex + "_" + loginNameFinal +
            //"_" + stringDescription + "_" + stringLongitude + "_" + stringLatitude
            String placeName = location_info_list[3];
            String longitude = location_info_list[4];
            String latitude = location_info_list[5];

            String stringTime = pref_time.getString("Time", "No time");
            String stringDescription = EditTextDescription.getText().toString();
            String stringDate = pref_date.getString("Date", "No date");
            String counter = pref_counter.getString("Event_counter", "1");



            reminderInfo.put("Longitude", longitude);
            reminderInfo.put("Latitude", latitude);
            reminderInfo.put("Index", counter);
            reminderInfo.put("Location", placeName);
            reminderInfo.put("Description", stringDescription);
            reminderInfo.put("Date", stringDate);
            reminderInfo.put("Time", stringTime);

            String reminderInfoJSON = gson.toJson(reminderInfo);
            pref.edit().putString(login_name + "_" + "reminder" + counter, reminderInfoJSON).apply();

            if(canCreateReminder)
            {
                scheduleWorker(stringDescription, stringDate, stringTime, counter);
                Log.v("AddReminderActivity" ,"Work created: " + workTag + counter);
                finish();
            }
        }
    }
}
