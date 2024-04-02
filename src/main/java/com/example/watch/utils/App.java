package com.example.watch.utils;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

public class App extends Application {
    private static Handler sHandler = null;
    private static Context sContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sHandler = new Handler();
        sContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return sContext;
    }

    public static Handler getsHandler() {
        return sHandler;
    }
}
