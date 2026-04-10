package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainHomeActivity extends AppCompatActivity {

    private ImageView ivHome, ivDiscover, ivRank, ivProfile;
    private TextView tvHome, tvDiscover, tvRank, tvProfile;
    private final int ACTIVE_COLOR = 0xFF6C5CE7; // Brand Purple
    private final int INACTIVE_COLOR = 0xFF888888; // Gray

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);

        // Initialize UI components for bottom nav
        ivHome = findViewById(R.id.ivNavHome);
        ivDiscover = findViewById(R.id.ivNavDiscover);
        ivRank = findViewById(R.id.ivNavLeaderboard);
        ivProfile = findViewById(R.id.ivNavProfile);

        tvHome = findViewById(R.id.tvNavHome);
        tvDiscover = findViewById(R.id.tvNavDiscover);
        tvRank = findViewById(R.id.tvNavLeaderboard);
        tvProfile = findViewById(R.id.tvNavProfile);

        // Click listeners for tabs
        findViewById(R.id.navHome).setOnClickListener(v -> loadFragment(new HomeFragment(), "HOME"));
        findViewById(R.id.navDiscover).setOnClickListener(v -> loadFragment(new DiscoverFragment(), "DISCOVER"));
        findViewById(R.id.navLeaderboard).setOnClickListener(v -> loadFragment(new RankFragment(), "RANK"));
        findViewById(R.id.navProfile).setOnClickListener(v -> loadFragment(new ProfileFragment(), "PROFILE"));

        // Center Quick Play Button
        findViewById(R.id.cardQuickPlay).setOnClickListener(v -> {
            Intent intent = new Intent(MainHomeActivity.this, QuizActivity.class);
            intent.putExtra("QUIZ_CATEGORY", "Quick Play");
            startActivity(intent);
        });

        // Load Default Fragment (Home)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "HOME");
        }
    }

    private void loadFragment(Fragment fragment, String tag) {
        // Highlight active tab
        updateNavUI(tag);

        // Swap Fragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment, tag);
        ft.commit();
    }

    private void updateNavUI(String tag) {
        // Reset all to inactive
        ivHome.setColorFilter(INACTIVE_COLOR);
        ivDiscover.setColorFilter(INACTIVE_COLOR);
        ivRank.setColorFilter(INACTIVE_COLOR);
        ivProfile.setColorFilter(INACTIVE_COLOR);

        tvHome.setTextColor(INACTIVE_COLOR);
        tvDiscover.setTextColor(INACTIVE_COLOR);
        tvRank.setTextColor(INACTIVE_COLOR);
        tvProfile.setTextColor(INACTIVE_COLOR);

        // Set active
        switch (tag) {
            case "HOME":
                ivHome.setColorFilter(ACTIVE_COLOR);
                tvHome.setTextColor(ACTIVE_COLOR);
                break;
            case "DISCOVER":
                ivDiscover.setColorFilter(ACTIVE_COLOR);
                tvDiscover.setTextColor(ACTIVE_COLOR);
                break;
            case "RANK":
                ivRank.setColorFilter(ACTIVE_COLOR);
                tvRank.setTextColor(ACTIVE_COLOR);
                break;
            case "PROFILE":
                ivProfile.setColorFilter(ACTIVE_COLOR);
                tvProfile.setTextColor(ACTIVE_COLOR);
                break;
        }
    }
}
