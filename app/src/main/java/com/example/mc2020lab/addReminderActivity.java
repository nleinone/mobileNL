package com.example.mc2020lab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class AddReminderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);
    }
    public void onClick(View v) {

        if (v.getId() == R.id.addReminderBtn) {

            Gson gson = new Gson();
            SharedPreferences pref = getApplicationContext().getSharedPreferences("reminder_info_preference", 0); // 0 - for private mode
            SharedPreferences pref_counter = getApplicationContext().getSharedPreferences("reminder_counter", 0); // 0 - for private mode

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

            EditText EditTextDescription = findViewById(R.id.editTextDesc);
            EditText editTextDays = findViewById(R.id.editTextDays);
            EditText editTextMonths = findViewById(R.id.editTextMonth);
            EditText editTextHours = findViewById(R.id.editTextHours);
            EditText editTextMins = findViewById(R.id.editTextMin);

            String stringDescription = EditTextDescription.getText().toString();
            String stringDays = editTextDays.getText().toString();
            String stringMonths = editTextMonths.getText().toString();
            String stringHours = editTextHours.getText().toString();
            String stringMins = editTextMins.getText().toString();

            Map<String, String> reminderInfo = new HashMap<String, String>();

            String counter = pref_counter.getString("Event_counter", "1");

            reminderInfo.put("Description", stringDescription);
            reminderInfo.put("Day", stringDays);
            reminderInfo.put("Month", stringMonths);
            reminderInfo.put("Hour", stringHours);
            reminderInfo.put("Min", stringMins);

            String reminderInfoJSON = gson.toJson(reminderInfo);
            pref.edit().putString("reminder" + counter, reminderInfoJSON).apply();

            finish();
        }
    }
}
