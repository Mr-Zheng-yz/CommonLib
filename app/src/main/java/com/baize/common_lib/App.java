package com.baize.common_lib;

import android.app.Application;

import com.ocamara.common_libs.utils.FileLogger;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FileLogger.getInstance().init(this);
    }
}
