package com.example.onlineexamapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import android.app.AlertDialog;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 🔙 Back Button
        ImageView ivBackSettings = findViewById(R.id.ivBackSettings);
        if (ivBackSettings != null) {
            ivBackSettings.setOnClickListener(v -> finish());
        }

        // ==========================================
        // 🌙 1. Dark Mode (With Dynamic Icon)
        // ==========================================
        SwitchCompat switchDarkMode = findViewById(R.id.switchDarkMode);
        ImageView ivThemeIcon = findViewById(R.id.ivThemeIcon);
        
        if (switchDarkMode != null && ivThemeIcon != null) {
            boolean isDark = ThemeHelper.isDarkMode(this);
            switchDarkMode.setChecked(isDark);
            ivThemeIcon.setImageResource(isDark ? R.drawable.ic_moon : R.drawable.ic_sun);
            
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Save and Apply
                ThemeHelper.saveTheme(this, isChecked);
                
                // Update Icon instantly
                ivThemeIcon.setImageResource(isChecked ? R.drawable.ic_moon : R.drawable.ic_sun);
                
                String mode = isChecked ? "Dark Mode Enabled" : "Light Mode Enabled";
                Toast.makeText(this, mode, Toast.LENGTH_SHORT).show();
            });
        }

        // ==========================================
        // 🎵 2. Sound & Vibration (With Persistence)
        // ==========================================
        SharedPreferences prefs = getSharedPreferences("MindSpaceSettings", MODE_PRIVATE);

        SwitchCompat switchSound = findViewById(R.id.switchSound);
        if (switchSound != null) {
            switchSound.setChecked(prefs.getBoolean("quiz_sound", true));
            switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("quiz_sound", isChecked).apply();
                String msg = isChecked ? "Correct Sound ON 🔊" : "Correct Sound OFF 🔇";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        }

        // ==========================================
        // 📳 3. Vibration Switch (With Persistence)
        // ==========================================
        SwitchCompat switchVibration = findViewById(R.id.switchVibration);
        if (switchVibration != null) {
            switchVibration.setChecked(prefs.getBoolean("vibration_enabled", true));
            switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("vibration_enabled", isChecked).apply();
                String msg = isChecked ? "Vibration ON 📳" : "Vibration OFF 🔕";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        }


    }
}