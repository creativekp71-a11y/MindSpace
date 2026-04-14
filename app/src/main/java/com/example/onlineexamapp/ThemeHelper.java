package com.example.onlineexamapp;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_DARK_MODE = "is_dark_mode";

    public static void saveTheme(Context context, boolean isDarkMode) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(KEY_DARK_MODE, isDarkMode);
        editor.apply();
        applyTheme(isDarkMode);
    }

    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_MODE, false); // Default to light mode
    }

    public static void applyTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
