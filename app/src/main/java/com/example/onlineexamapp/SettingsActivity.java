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
        // 🌙 1. Dark Mode का असली जादू (With Persistence)
        // ==========================================
        SwitchCompat switchDarkMode = findViewById(R.id.switchDarkMode);
        if (switchDarkMode != null) {
            // Load current state
            switchDarkMode.setChecked(ThemeHelper.isDarkMode(this));
            
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Save and Apply
                ThemeHelper.saveTheme(this, isChecked);
                
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
            switchSound.setChecked(prefs.getBoolean("sound_enabled", true));
            switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("sound_enabled", isChecked).apply();
                String msg = isChecked ? "Sounds ON 🔊" : "Sounds OFF 🔇";
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

        // ==========================================
        // 🌍 3. Language Selector (मस्त पॉपअप के साथ)
        // ==========================================
        RelativeLayout layoutLanguage = findViewById(R.id.layoutLanguage);
        if (layoutLanguage != null) {
            layoutLanguage.setOnClickListener(v -> {
                // भाषाओं की लिस्ट
                String[] languages = {"English", "ગુજરાતી"};

                // अलर्ट डायलॉग (पॉपअप) बनाएँ
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Select Language")
                        .setItems(languages, (dialog, which) -> {
                            if (which == 0) {
                                Toast.makeText(SettingsActivity.this, "Language set to English", Toast.LENGTH_SHORT).show();
                                // (भविष्य में यहाँ इंग्लिश सेट करने का कोड आएगा)
                            } else if (which == 1) {
                                Toast.makeText(SettingsActivity.this, "ભાષા ગુજરાતી પસંદ કરવામાં આવી છે", Toast.LENGTH_SHORT).show();
                                // (भविष्य में यहाँ गुजराती सेट करने का कोड आएगा)
                            }
                        })
                        .show();
            });
        }
    }
}