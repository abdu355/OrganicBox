package com.example.b00047562.organicbox;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Administrator on 12/19/2015.
 */
public class ParseApp extends Application {
    public static final String YOUR_APPLICATION_ID = "KsHGj0HWmhbnDQ5A4RnsMyyUZhj75k2sx9C2ROAT";
    public static final String YOUR_CLIENT_KEY = "YJNsdjs9DwluxTiveIEFDT3sCenb4uuwkHkq3sGt";

    @Override
    public void onCreate() {
        super.onCreate();

        // Add your initialization code here
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, YOUR_APPLICATION_ID, YOUR_CLIENT_KEY);

    }
}
