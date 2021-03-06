package com.example.b00047562.organicbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotService", "Boot completed");

        // start service
        Intent service = new Intent(context, NotificationService.class);
        context.startService(service);
    }
}
