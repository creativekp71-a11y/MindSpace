package com.example.onlineexamapp;

import android.app.Application;

public class MindSpaceApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Apply saved theme preference at startup
        boolean isDarkMode = ThemeHelper.isDarkMode(this);
        ThemeHelper.applyTheme(isDarkMode);
    }
}
