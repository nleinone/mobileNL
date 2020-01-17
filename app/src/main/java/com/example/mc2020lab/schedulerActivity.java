package com.example.mc2020lab;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class SchedulerActivity extends AppCompatActivity {

    public void setTextToRow(String text, TableRow row, TableLayout table, String index)
    {

        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TextView descTxt=new TextView(this);
        descTxt.setText(text);
        descTxt.setPadding(60, 5, 5, 5);

        TextView t2=new TextView(this);
        t2.setText("E");

        Button deleteButton = new Button(this);
        final String index_final = index;
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                SharedPreferences pref = getApplicationContext().getSharedPreferences("reminder_info_preference", 0); // 0 - for private mode
                pref.edit().remove("reminder" + index_final).apply();
                checkAndLoadReminders();
            }
        });

        Button editButton = new Button(this);
        final String index_final2 = index;
        editButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                //Create input box:
                //Ref: https://stackoverflow.com/questions/18799216/how-to-make-a-edittext-box-in-a-dialog

                //Create alert box:
                AlertDialog.Builder alert = new AlertDialog.Builder(SchedulerActivity.this);

                //Create viewGroup for multiple views in the alert box:

                //ADD LAYOUTS FOR EVERY INFO:
                Context context = SchedulerActivity.this;
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                //Create Description editText and TextView to alert box:
                final EditText editTextDesct = new EditText(SchedulerActivity.this);
                final TextView txDesc = new TextView((SchedulerActivity.this));
                txDesc.setText("Description: ");

                //Create Day editText and TextView to alert box:
                final EditText editTextDay = new EditText(SchedulerActivity.this);
                final TextView txDay = new TextView((SchedulerActivity.this));
                txDesc.setText("Day: ");

                //Create Month editText and TextView to alert box:
                final EditText editTextMonth = new EditText(SchedulerActivity.this);
                final TextView txMonth = new TextView((SchedulerActivity.this));
                txDesc.setText("Month: ");

                //Create Hour editText and TextView to alert box:
                final EditText editTextHour = new EditText(SchedulerActivity.this);
                final TextView txHour = new TextView((SchedulerActivity.this));
                txDesc.setText("Hour: ");

                //Create Hour editText and TextView to alert box:
                final EditText editTextMin = new EditText(SchedulerActivity.this);
                final TextView txMin = new TextView((SchedulerActivity.this));
                txDesc.setText("Min: ");

                layout.addView(txDesc);
                layout.addView(editTextDesct);

                layout.addView(txDay);
                layout.addView(editTextDay);

                layout.addView(txMonth);
                layout.addView(editTextMonth);

                layout.addView(txHour);
                layout.addView(editTextHour);

                layout.addView(txMin);
                layout.addView(editTextMin);

                //Set texts to alert box
                alert.setMessage("Edit reminder values");
                alert.setTitle("Edit");

                alert.setView(layout);

                //Create positive button
                alert.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Get string:
                        String stringDescription = editTextDesct.getText().toString();
                        //String stringDays;
                        //String stringMonths;
                        //String stringHours;
                        //String stringMins;

                        SharedPreferences pref = getApplicationContext().getSharedPreferences("reminder_info_preference", 0); // 0 - for private mode
                        Map<String, String> reminderInfo = new HashMap<String, String>();
                        reminderInfo.put("Description", stringDescription);
                        //reminderInfo.put("Day", stringDays);
                        //reminderInfo.put("Month", stringMonths);
                        //reminderInfo.put("Hour", stringHours);
                        //reminderInfo.put("Min", stringMins);

                        Gson gson = new Gson();
                        String reminderInfoJSON = gson.toJson(reminderInfo);
                        pref.edit().putString("reminder" + index_final, reminderInfoJSON).apply();
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

        //android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,100); // 60 is height you can set it as u need

        //deleteButton.setLayoutParams(lp);
        //TextView t3=new TextView(this);
        //t3.setText("D");
        TextView t4=new TextView(this);
        t4.setText(index);

        row.addView(descTxt);
        row.addView(editButton);
        row.addView(deleteButton);
        row.addView(t4);
        table.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    public void checkAndLoadReminders()
    {
        Gson gson = new Gson();

        TableLayout table = (TableLayout) findViewById(R.id.tableLayout);
        table.removeAllViews();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("reminder_info_preference", 0); // 0 - for private mode

        for (int i = 1; i < 12; i++) {
            String str = Integer.toString(i);

            if (pref.contains("reminder" + str)) {

                String storedHashMapString = pref.getString("reminder" + str, "oopsDintWork");
                java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
                }.getType();

                HashMap<String, String> reminder_information = gson.fromJson(storedHashMapString, type);

                String description = reminder_information.get("Description");
                String days = reminder_information.get("Day");
                String months = reminder_information.get("Month");
                String hours = reminder_information.get("Hour");
                String mins = reminder_information.get("Min");

                SharedPreferences pref_counter = getApplicationContext().getSharedPreferences("reminder_counter", 0); // 0 - for private mode
                String index = Integer.toString(i);
                //String event_counter = pref_counter.getString("Event_counter", "1");
                //String event_counter_string =  event_counter + "Reminder";
                //String index = pref.getString(event_counter_string, "");

                //https://stackoverflow.com/questions/11342975/android-create-textviews-in-tablerows-programmatically

                TableRow row1 = new TableRow(this);

                setTextToRow(description, row1, table, index);
                //setTextToRow(days, table, row1);
                //setTextToRow(months, table, row1);
                //setTextToRow(hours, table, row1);
                //setTextToRow(mins, table, row1);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);

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
