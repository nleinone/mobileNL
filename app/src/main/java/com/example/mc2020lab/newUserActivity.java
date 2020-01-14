package com.example.mc2020lab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class NewUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
    }
    public void onClick(View v) {

        if (v.getId() == R.id.newUserRegisterBtn) {
            startActivity(new Intent(NewUserActivity.this, MainActivity.class));
        }
    }
}
