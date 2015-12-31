package com.example.b00047562.organicbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("News reader", "Connectivity changed");

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo =
                connectivityManager.getActiveNetworkInfo();

        Intent service = new Intent(context, NotificationService.class);

        if (networkInfo != null && networkInfo.isConnected()){
            Log.d("NotService", "Connected");
            context.startService(service);
        }
        else {
            Log.d("NotService", "NOT connected");
            context.stopService(service);
        }
    }
}
