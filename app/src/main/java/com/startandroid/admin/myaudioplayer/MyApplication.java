package com.startandroid.admin.myaudioplayer;

import android.app.Application;


public class MyApplication extends Application {

    private static Application CONTEXT;

    @Override
    public void onCreate() {
        super.onCreate();
        CONTEXT = this;
    }

    public static Application getContext(){
        return CONTEXT;
    }
}
