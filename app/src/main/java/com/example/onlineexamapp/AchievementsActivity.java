package com.example.onlineexamapp; // 👈 अपने पैकेज का नाम चेक कर लेना

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AchievementsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        // ==========================================
        // 🏅 Saved Points को स्क्रीन पर दिखाना
        // ==========================================
        android.content.SharedPreferences prefs = getSharedPreferences("MindSpacePrefs", MODE_PRIVATE);
        int totalPoints = prefs.getInt("total_points", 950);

        TextView tvCurrentPoints = findViewById(R.id.tvPoints);

        if (tvCurrentPoints != null) {
            tvCurrentPoints.setText(totalPoints + " Points");

            if(totalPoints >= 1000) {
                Toast.makeText(this, "You reached Silver Level! 🥈", Toast.LENGTH_LONG).show();
            }
        }

        // ==========================================
        // 🔙 Back Button (Left Arrow) का कनेक्शन
        // ==========================================
        ImageView ivBackAchievements = findViewById(R.id.ivBackAchievements);

        if (ivBackAchievements != null) {
            ivBackAchievements.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // इस पेज को बंद कर देगा और यूज़र पिछले पेज पर चला जाएगा
                    finish();
                }
            });
        }

    } // 👈 onCreate का ब्रैकेट यहाँ खत्म हो रहा है, सब कुछ इसके अंदर है!
}