package com.example.mc2020lab;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import java.util.Map;
import 	java.util.Calendar;

public class AddReminderActivity extends AppCompatActivity {

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
            TextView tvLoc = findViewById(R.id.tvLocation);

            //Get location with placePicker


            String stringLoc = "";
            tvLoc.setText(stringLoc);
            SharedPreferences pref_time = getApplicationContext().getSharedPreferences("pref_loc", 0); // 0 - for private mode
            pref_time.edit().putString("Location", stringLoc).apply();

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
            SharedPreferences pref_loc = getApplicationContext().getSharedPreferences("pref_time", 0); // 0 - for private mode


            String stringTime = pref_time.getString("Time", "No time");
            String stringDescription = EditTextDescription.getText().toString();
            String stringDate = pref_date.getString("Date", "No date");
            String stringLoc = pref_date.getString("Location", "No loc");

            String counter = pref_counter.getString("Event_counter", "1");

            reminderInfo.put("Description", stringDescription);
            reminderInfo.put("Date", stringDate);
            reminderInfo.put("Time", stringTime);
            reminderInfo.put("Location", stringLoc);

            String reminderInfoJSON = gson.toJson(reminderInfo);
            pref.edit().putString(login_name + "_" + "reminder" + counter, reminderInfoJSON).apply();

            if(canCreateReminder)
            {
                finish();
            }
        }
    }
}
