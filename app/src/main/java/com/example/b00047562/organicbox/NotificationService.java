package com.example.b00047562.organicbox;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {
    private ParseApp app;
    private Timer timer;


    public NotificationService() {
    }

    @Override
    public void onCreate() {


        Log.d("NotService", "Service created");
        app = (ParseApp) getApplication();
        startTimer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("NotService", "Service started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("NotService", "Service bound - not used!");
        return null;
    }


    @Override
    public void onDestroy() {
        Log.d("NotService", "Service destroyed");
        stopTimer();
    }

    private void startTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                queryServer();
            }
        };

        timer = new Timer(true);
        int delay = 1000 * 5;      // 5 sec
        int interval = 1000 * 60;   // 60 seconds
        timer.schedule(task, delay, interval);

    }
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }


    private void sendNotification(String text) {
        // create the intent for the notification
        Intent notificationIntent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // create the pending intent
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, flags);

        // create the variables for the notification
        int icon = R.drawable.organicboxlogo;
        CharSequence tickerText = "Shipped Items Available";
        CharSequence contentTitle = getText(R.string.app_name);
        CharSequence contentText = text;

        // create the notification and set its data
        Notification notification =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(icon)
                        .setTicker(tickerText)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

        // display the notification
        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        final int NOTIFICATION_ID = 1;
        manager.notify(NOTIFICATION_ID, notification);
    }

    private void queryServer() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Orders");
        query.whereEqualTo("createdBy", ParseUser.getCurrentUser());
        query.whereEqualTo("tracker_status","Shipping");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null) {

                    if(objects.size()>0)
                    {
                        sendNotification(objects.size()+" Item(s) have been Shipped");
                    }
                    else {

                    }

                }
                else {

                }
            }
        });
    }
}


