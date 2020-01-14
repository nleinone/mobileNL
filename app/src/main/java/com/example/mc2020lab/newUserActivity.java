package com.example.mc2020lab;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class NewUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
    }
    public void onClick(View v) {

        if (v.getId() == R.id.newUserRegisterBtn) {

            //When the button is pressed, test if the username already exists in the device:

            //Check if the password is short enough (4)

            //Find EditTexts
            EditText usernameInput = findViewById(R.id.newUserNameTxt);
            EditText passwordInput = findViewById(R.id.newUserPasswordTxt);
            EditText passwordInput2 = findViewById(R.id.newUserPassword2Txt);

            //Get inputs
            String username_string = usernameInput.getText().toString();
            String password_string = passwordInput.getText().toString();
            String password2_string = passwordInput2.getText().toString();

            DialogInterface.OnClickListener buttonListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    };


            Gson gson = new Gson();
            SharedPreferences pref = getApplicationContext().getSharedPreferences("shared_preference", 0); // 0 - for private mode

            //Get current shared preference
            String storedHashMapString = pref.getString("user_information", "oopsDintWork");
            java.lang.reflect.Type type = new TypeToken<HashMap<String, Map<String, String>>>(){}.getType();
            HashMap<String, Map<String, String>> user_information = gson.fromJson(storedHashMapString, type);

            //Check if username exists:
            if(user_information.containsKey(username_string))
            {
                AlertDialog introDialog = new AlertDialog.Builder(this)
                        .setTitle("Invalid Username!")
                        .setMessage("Username already exists!")
                        .setPositiveButton("Ok", buttonListener)
                        .setCancelable(false)
                        .create();
                introDialog.show();
            }

            //Check input validity:
            else if(username_string.length() > 8 || username_string.length() < 4)
            {
                AlertDialog introDialog = new AlertDialog.Builder(this)
                        .setTitle("Invalid Username!")
                        .setMessage("Username must be between 4-8 long!")
                        .setPositiveButton("Ok", buttonListener)
                        .setCancelable(false)
                        .create();
                introDialog.show();
            }

            else if(password_string.length() > 4 || password2_string.length() > 4)
            {
                AlertDialog introDialog = new AlertDialog.Builder(this)
                        .setTitle("Invalid password!")
                        .setMessage("Password must be 4 characters long!")
                        .setPositiveButton("Ok", buttonListener)
                        .setCancelable(false)
                        .create();
                introDialog.show();
            }

            else if(!(password_string.equals(password2_string)))
            {
                AlertDialog introDialog = new AlertDialog.Builder(this)
                        .setTitle("Invalid password!")
                        .setMessage("Passwords does not match!")
                        .setPositiveButton("Ok", buttonListener)
                        .setCancelable(false)
                        .create();
                introDialog.show();
            }

            else
            {

                Toast.makeText(getApplicationContext(), "Account created!", Toast.LENGTH_SHORT).show();
                //Add username to shared preference:
                Map<String, String> userInfoDict = new HashMap<String, String>();
                userInfoDict.put("password", password_string);

                Map<String, Map<String, String>> rootUserInfoDict = new HashMap<String, Map<String, String>>();
                rootUserInfoDict.put(username_string, userInfoDict);

                //Convert to JSON and insert JSON string to shared Reference:
                //https://stackoverflow.com/questions/37048731/gson-library-in-android-studio/37049457

                String JsonStringUserData = gson.toJson(rootUserInfoDict);
                pref.edit().putString("user_information", JsonStringUserData).apply();
                //startActivity(new Intent(NewUserActivity.this, MainActivity.class));
                finish();

            }

        }
    }




}
