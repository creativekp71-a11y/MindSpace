package com.example.onlineexamapp;

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
        // 🌙 1. Dark Mode का असली जादू
        // ==========================================
        SwitchCompat switchDarkMode = findViewById(R.id.switchDarkMode);
        if (switchDarkMode != null) {
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // पूरे ऐप को डार्क मोड में डालो
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    // पूरे ऐप को लाइट मोड में डालो
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            });
        }

        // ==========================================
        // 🎵 2. Sound Switch (अभी के लिए Toast)
        // ==========================================
        SwitchCompat switchSound = findViewById(R.id.switchSound);
        if (switchSound != null) {
            switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String msg = isChecked ? "Sounds ON 🔊" : "Sounds OFF 🔇";
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