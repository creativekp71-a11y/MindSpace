package com.example.onlineexamapp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.facebook.shimmer.ShimmerFrameLayout;
import android.view.View;
import android.widget.ScrollView;

public class AchievementsActivity extends AppCompatActivity {

    private ImageView ivBackAchievements;

    private TextView tvPoints;
    private TextView tvCurrentLevelIcon;
    private TextView tvCurrentLevelName;
    private TextView tvNextTarget;
    private TextView tvProgressMessage;

    private TextView tvBronzeStatus;
    private TextView tvSilverStatus;
    private TextView tvGoldStatus;
    private TextView tvDiamondStatus;

    private ProgressBar pbLevelProgress;

    private ShimmerFrameLayout shimmerAchievements;
    private ScrollView svAchievementsContent;
    private View llAchievementsDynamicContent;

    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        currentUid = mAuth.getUid();

        ivBackAchievements = findViewById(R.id.ivBackAchievements);

        tvPoints = findViewById(R.id.tvPoints);
        tvCurrentLevelIcon = findViewById(R.id.tvCurrentLevelIcon);
        tvCurrentLevelName = findViewById(R.id.tvCurrentLevelName);
        tvNextTarget = findViewById(R.id.tvNextTarget);
        tvProgressMessage = findViewById(R.id.tvProgressMessage);

        tvBronzeStatus = findViewById(R.id.tvBronzeStatus);
        tvSilverStatus = findViewById(R.id.tvSilverStatus);
        tvGoldStatus = findViewById(R.id.tvGoldStatus);
        tvDiamondStatus = findViewById(R.id.tvDiamondStatus);

        pbLevelProgress = findViewById(R.id.pbLevelProgress);

        shimmerAchievements = findViewById(R.id.shimmer_achievements);
        svAchievementsContent = findViewById(R.id.svAchievementsContent);
        llAchievementsDynamicContent = findViewById(R.id.llAchievementsDynamicContent);

        // Start Shimmer
        shimmerAchievements.startShimmer();

        ivBackAchievements.setOnClickListener(v -> finish());

        loadUserPoints();
    }

    private void loadUserPoints() {
        if (currentUid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        fStore.collection("Users")
                .document(currentUid)
                .get()
                .addOnSuccessListener(this::applyAchievementData)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load achievements", Toast.LENGTH_SHORT).show());
    }

    private void applyAchievementData(@NonNull DocumentSnapshot documentSnapshot) {
        Long pointsLong = documentSnapshot.getLong("points");
        int points = pointsLong != null ? pointsLong.intValue() : 0;

        // Negative points ko UI me 0 treat karenge
        if (points < 0) {
            points = 0;
        }

        tvPoints.setText(points + " Points");
        resetTierStatus();

        // Stop Shimmer and show content
        shimmerAchievements.stopShimmer();
        shimmerAchievements.setVisibility(View.GONE);
        if (llAchievementsDynamicContent != null) {
            llAchievementsDynamicContent.setVisibility(View.VISIBLE);
        }

        // Bronze: 0 - 49
        if (points < 50) {
            showBronze(points);
        }
        // Silver: 50 - 99
        else if (points < 100) {
            showSilver(points);
        }
        // Gold: 100 - 199
        else if (points < 200) {
            showGold(points);
        }
        // Diamond: 200+
        else {
            showDiamond(points);
        }
    }

    private void showBronze(int points) {
        tvCurrentLevelIcon.setText("🥉");
        tvCurrentLevelName.setText("Bronze Beginner");
        tvNextTarget.setText("50 (Silver)");

        pbLevelProgress.setMax(50);
        pbLevelProgress.setProgress(points);

        int remaining = 50 - points;
        tvProgressMessage.setText("Almost there! Only " + remaining + " points needed for Silver! 🔥");

        tvBronzeStatus.setText("📍 Current");
        tvBronzeStatus.setTextColor(Color.parseColor("#E17055"));
    }

    private void showSilver(int points) {
        tvCurrentLevelIcon.setText("🥈");
        tvCurrentLevelName.setText("Silver Pro");
        tvNextTarget.setText("100 (Gold)");

        pbLevelProgress.setMax(50);
        pbLevelProgress.setProgress(points - 50);

        int remaining = 100 - points;
        tvProgressMessage.setText("Nice! Only " + remaining + " points needed for Gold! 🚀");

        tvBronzeStatus.setText("✅ Unlocked");
        tvBronzeStatus.setTextColor(Color.parseColor("#27AE60"));

        tvSilverStatus.setText("📍 Current");
        tvSilverStatus.setTextColor(Color.parseColor("#6C5CE7"));
    }

    private void showGold(int points) {
        tvCurrentLevelIcon.setText("🥇");
        tvCurrentLevelName.setText("Gold Master");
        tvNextTarget.setText("200 (Diamond)");

        pbLevelProgress.setMax(100);
        pbLevelProgress.setProgress(points - 100);

        int remaining = 200 - points;
        tvProgressMessage.setText("Great job! Only " + remaining + " points needed for Diamond! ✨");

        tvBronzeStatus.setText("✅ Unlocked");
        tvBronzeStatus.setTextColor(Color.parseColor("#27AE60"));

        tvSilverStatus.setText("✅ Unlocked");
        tvSilverStatus.setTextColor(Color.parseColor("#27AE60"));

        tvGoldStatus.setText("📍 Current");
        tvGoldStatus.setTextColor(Color.parseColor("#F39C12"));
    }

    private void showDiamond(int points) {
        tvCurrentLevelIcon.setText("💎");
        tvCurrentLevelName.setText("Diamond Legend");
        tvNextTarget.setText("MAX LEVEL");

        // 200 ke baad bhi bar full/fullest dikhe
        pbLevelProgress.setMax(100);
        pbLevelProgress.setProgress(100);

        tvProgressMessage.setText("Amazing! You reached Diamond Legend! 👑");

        tvBronzeStatus.setText("✅ Unlocked");
        tvBronzeStatus.setTextColor(Color.parseColor("#27AE60"));

        tvSilverStatus.setText("✅ Unlocked");
        tvSilverStatus.setTextColor(Color.parseColor("#27AE60"));

        tvGoldStatus.setText("✅ Unlocked");
        tvGoldStatus.setTextColor(Color.parseColor("#27AE60"));

        tvDiamondStatus.setText("📍 Current");
        tvDiamondStatus.setTextColor(Color.parseColor("#00A8FF"));
    }

    private void resetTierStatus() {
        tvBronzeStatus.setText("🔒 Locked");
        tvSilverStatus.setText("🔒 Locked");
        tvGoldStatus.setText("🔒 Locked");
        tvDiamondStatus.setText("🔒 Locked");

        tvBronzeStatus.setTextColor(Color.parseColor("#A4B0BE"));
        tvSilverStatus.setTextColor(Color.parseColor("#A4B0BE"));
        tvGoldStatus.setTextColor(Color.parseColor("#A4B0BE"));
        tvDiamondStatus.setTextColor(Color.parseColor("#A4B0BE"));
    }
}