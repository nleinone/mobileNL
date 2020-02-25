package com.example.mc2020lab;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

//REF: https://developer.android.com/topic/libraries/architecture/workmanager/basics.html
//https://medium.com/android-ideas/scheduling-notifications-on-android-with-workmanager-6d84b7f64613
//https://inducesmile.com/android/schedule-onetime-notification-with-android-workmanager/

public class UploadWorker extends Worker {

    public UploadWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {

        //Check
        SharedPreferences workerData = getApplicationContext().getSharedPreferences("WorkerData", 0);
        String workDesc = workerData.getString("TaskDesc", "No description");
        String id = workerData.getString("TaskId", "No Id");
        String title = "Reminder!";
        Log.v("Worker", "Work started!");
        sendNotification(workDesc, title, id);

        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }

    private void sendNotification(String workDesc, String title, String id) {

        //REF: https://stackoverflow.com/questions/46990995/on-android-8-1-api-27-notification-does-not-display
        //REF: https://developer.android.com/training/notify-user/build-notification
        //Create notification
        String channelId = "default_channel_id";
        Context context = getApplicationContext();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(workDesc)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        createNotificationChannel();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        int intId = Integer.parseInt(id);
        notificationManager.notify(intId, builder.build());


        Log.v("Worker", "Work done!");
        //Save notification id for later use:
        SharedPreferences pref = getApplicationContext().getSharedPreferences("reminder_info_preference", 0); // 0 - for private mode
        SharedPreferences login_name_pref = getApplicationContext().getSharedPreferences("login_name", 0); // 0 - for private mode
        String login_name = login_name_pref.getString("loginName", "NoName");

        pref.edit().putString(login_name + "_" + "notification" + "_" + id, id).apply();


    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "default_channel_id";
            String description = "default_channel_description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("default_channel_id", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            Context context = getApplicationContext();
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
