package com.example.mc2020lab;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    DialogInterface.OnClickListener buttonListener =
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };

    public void showAlert()
    {
        AlertDialog introDialog = new AlertDialog.Builder(this)
                .setTitle("Invalid Username!")
                .setMessage("Username does not exist!")
                .setPositiveButton("Ok", buttonListener)
                .setCancelable(false)
                .create();
        introDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check current shared reference for reminders:
        //Get current shared preference

    }

    public void onClick(View v) {

        if (v.getId() == R.id.loginBtn) {

            //Check if credentials are correct:
            Gson gson = new Gson();
            SharedPreferences pref = getApplicationContext().getSharedPreferences("shared_preference", 0); // 0 - for private mode

            //Clear memory
            //pref.edit().clear().apply();




            //Get input
            EditText username = findViewById(R.id.loginNameTxt);
            EditText password = findViewById(R.id.loginPasswordTxt);

            String loginname = username.getText().toString();
            String loginPassword = password.getText().toString();

            String storedHashMapString = pref.getString( loginname + "_" + "user_information", "oopsDintWork");

            Log.v("Main", storedHashMapString);

            if(!(storedHashMapString.equals("oopsDintWork"))) {
                //Check the shared preference:
                java.lang.reflect.Type type = new TypeToken<HashMap<String, Map<String, String>>>() {
                }.getType();
                HashMap<String, Map<String, String>> user_information = gson.fromJson(storedHashMapString, type);

                if (user_information.containsKey(loginname)) {
                    Map<String, String> password_dict = user_information.get(loginname);
                    String user_password = password_dict.get("password");

                    //Check if the password match:
                    if (loginPassword.equals(user_password))
                    {
                        Toast.makeText(getApplicationContext(), "Logged in!", Toast.LENGTH_SHORT).show();

                        //Save login name to pref for next activity:
                        SharedPreferences login_name_pref = getApplicationContext().getSharedPreferences("login_name", 0); // 0 - for private mode
                        login_name_pref.edit().putString("loginName", loginname).apply();

                        //user_information.put(loginname, password_dict);

                        //Turn HashMap to Json string, insert it to shared pref:
                        //String jsonStringUserinformation = gson.toJson(user_information);
                        //pref.edit().putString("user_information", jsonStringUserinformation).apply();

                        startActivity(new Intent(MainActivity.this, SchedulerActivity.class));
                    }

                    else
                    {
                        AlertDialog introDialog = new AlertDialog.Builder(this)
                                .setTitle("Invalid password!")
                                .setMessage("Password is incorrect!")
                                .setPositiveButton("Ok", buttonListener)
                                .setCancelable(false)
                                .create();
                        introDialog.show();
                    }
                }
                else
                {
                    showAlert();
                }
            }
            else
            {
                showAlert();
            }
        }

        else if (v.getId() == R.id.loginNewUserBtn) {
            startActivity(new Intent(MainActivity.this, NewUserActivity.class));

        }
    }
}