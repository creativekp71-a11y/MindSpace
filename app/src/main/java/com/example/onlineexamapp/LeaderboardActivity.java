package com.example.onlineexamapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class LeaderboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        ImageView ivBackLeaderboard = findViewById(R.id.ivBackLeaderboard);
        if (ivBackLeaderboard != null) {
            ivBackLeaderboard.setOnClickListener(v -> finish());
        }
    }
}