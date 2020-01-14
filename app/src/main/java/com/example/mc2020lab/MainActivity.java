package com.example.mc2020lab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {

        if (v.getId() == R.id.loginBtn) {
            startActivity(new Intent(MainActivity.this, SchedulerActivity.class));

        }

        else if (v.getId() == R.id.loginNewUserBtn) {
            startActivity(new Intent(MainActivity.this, NewUserActivity.class));

        }
    }
}