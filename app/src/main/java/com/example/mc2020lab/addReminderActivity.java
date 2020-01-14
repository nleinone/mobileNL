package com.example.mc2020lab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AddReminderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);
    }
    public void onClick(View v) {

        if (v.getId() == R.id.addReminderBtn) {
            startActivity(new Intent(AddReminderActivity.this, SchedulerActivity.class));

        }
    }
}
