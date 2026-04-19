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
    private TextView tvBronzeTitle, tvBronzePoints, tvSilverTitle, tvSilverPoints, tvGoldTitle, tvGoldPoints, tvDiamondTitle, tvDiamondPoints;
    private com.google.android.material.card.MaterialCardView cvBronzeCard, cvSilverCard, cvGoldCard, cvDiamondCard;

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

        cvBronzeCard = findViewById(R.id.cvBronzeCard);
        cvSilverCard = findViewById(R.id.cvSilverCard);
        cvGoldCard = findViewById(R.id.cvGoldCard);
        cvDiamondCard = findViewById(R.id.cvDiamondCard);

        tvBronzeTitle = findViewById(R.id.tvBronzeTitle);
        tvBronzePoints = findViewById(R.id.tvBronzePoints);
        tvSilverTitle = findViewById(R.id.tvSilverTitle);
        tvSilverPoints = findViewById(R.id.tvSilverPoints);
        tvGoldTitle = findViewById(R.id.tvGoldTitle);
        tvGoldPoints = findViewById(R.id.tvGoldPoints);
        tvDiamondTitle = findViewById(R.id.tvDiamondTitle);
        tvDiamondPoints = findViewById(R.id.tvDiamondPoints);

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
        // Apply premium shiny effect to badge icon
        applyPremiumBadgeEffect();
    }

    private void applyPremiumBadgeEffect() {
        tvCurrentLevelIcon.post(() -> {
            float width = tvCurrentLevelIcon.getWidth();
            float height = tvCurrentLevelIcon.getHeight();
            if (width > 0 && height > 0) {
                // Natural rich bronze/gold gradient
                android.graphics.Shader textShader = new android.graphics.LinearGradient(0, 0, 0, height,
                        new int[]{
                                Color.parseColor("#CD7F32"), // Rich Bronze
                                Color.parseColor("#8B4513")  // Saturated Brown
                        }, null, android.graphics.Shader.TileMode.CLAMP);
                tvCurrentLevelIcon.getPaint().setShader(textShader);
                
                // Remove glow effect as requested
                tvCurrentLevelIcon.setShadowLayer(0, 0, 0, 0); 
                tvCurrentLevelIcon.invalidate();
            }
        });
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
        tvBronzeStatus.setTextColor(Color.WHITE);
        tvBronzeTitle.setTextColor(Color.WHITE);
        tvBronzePoints.setTextColor(Color.WHITE);
        cvBronzeCard.setCardBackgroundColor(Color.parseColor("#6C5CE7"));
        cvBronzeCard.setAlpha(1.0f);
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
        tvBronzeStatus.setTextColor(Color.WHITE);
        tvBronzeTitle.setTextColor(Color.WHITE);
        tvBronzePoints.setTextColor(Color.WHITE);
        cvBronzeCard.setCardBackgroundColor(Color.parseColor("#6C5CE7"));
        cvBronzeCard.setAlpha(1.0f);

        tvSilverStatus.setText("📍 Current");
        tvSilverStatus.setTextColor(Color.WHITE);
        tvSilverTitle.setTextColor(Color.WHITE);
        tvSilverPoints.setTextColor(Color.WHITE);
        cvSilverCard.setCardBackgroundColor(Color.parseColor("#6C5CE7"));
        cvSilverCard.setAlpha(1.0f);
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
        tvBronzeStatus.setTextColor(Color.WHITE);
        tvBronzeTitle.setTextColor(Color.WHITE);
        tvBronzePoints.setTextColor(Color.WHITE);
        cvBronzeCard.setCardBackgroundColor(Color.parseColor("#6C5CE7"));
        cvBronzeCard.setAlpha(1.0f);

        tvSilverStatus.setText("✅ Unlocked");
        tvSilverStatus.setTextColor(Color.WHITE);
        tvSilverTitle.setTextColor(Color.WHITE);
        tvSilverPoints.setTextColor(Color.WHITE);
        cvSilverCard.setCardBackgroundColor(Color.parseColor("#6C5CE7"));
        cvSilverCard.setAlpha(1.0f);

        tvGoldStatus.setText("📍 Current");
        tvGoldStatus.setTextColor(Color.WHITE);
        tvGoldTitle.setTextColor(Color.WHITE);
        tvGoldPoints.setTextColor(Color.WHITE);
        cvGoldCard.setCardBackgroundColor(Color.parseColor("#6C5CE7"));
        cvGoldCard.setAlpha(1.0f);
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
        tvBronzeStatus.setTextColor(Color.WHITE);
        tvBronzeTitle.setTextColor(Color.WHITE);
        tvBronzePoints.setTextColor(Color.WHITE);
        cvBronzeCard.setCardBackgroundColor(Color.parseColor("#6C5CE7"));
        cvBronzeCard.setAlpha(1.0f);

        tvSilverStatus.setText("✅ Unlocked");
        tvSilverStatus.setTextColor(Color.WHITE);
        tvSilverTitle.setTextColor(Color.WHITE);
        tvSilverPoints.setTextColor(Color.WHITE);
        cvSilverCard.setCardBackgroundColor(Color.parseColor("#6C5CE7"));
        cvSilverCard.setAlpha(1.0f);

        tvGoldStatus.setText("✅ Unlocked");
        tvGoldStatus.setTextColor(Color.WHITE);
        tvGoldTitle.setTextColor(Color.WHITE);
        tvGoldPoints.setTextColor(Color.WHITE);
        cvGoldCard.setCardBackgroundColor(Color.parseColor("#6C5CE7"));
        cvGoldCard.setAlpha(1.0f);

        tvDiamondStatus.setText("📍 Current");
        tvDiamondStatus.setTextColor(Color.WHITE);
        tvDiamondTitle.setTextColor(Color.WHITE);
        tvDiamondPoints.setTextColor(Color.WHITE);
        cvDiamondCard.setCardBackgroundColor(Color.parseColor("#6C5CE7"));
        cvDiamondCard.setAlpha(1.0f);
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

        tvBronzeTitle.setTextColor(Color.parseColor("#E17055"));
        tvSilverTitle.setTextColor(Color.parseColor("#A4B0BE"));
        tvGoldTitle.setTextColor(Color.parseColor("#A4B0BE"));
        tvDiamondTitle.setTextColor(Color.parseColor("#A4B0BE"));

        tvBronzePoints.setTextColor(Color.parseColor("#A4B0BE"));
        tvSilverPoints.setTextColor(Color.parseColor("#A4B0BE"));
        tvGoldPoints.setTextColor(Color.parseColor("#A4B0BE"));
        tvDiamondPoints.setTextColor(Color.parseColor("#A4B0BE"));

        int bgColor = getResources().getColor(R.color.card_background);
        cvBronzeCard.setCardBackgroundColor(bgColor);
        cvSilverCard.setCardBackgroundColor(bgColor);
        cvGoldCard.setCardBackgroundColor(bgColor);
        cvDiamondCard.setCardBackgroundColor(bgColor);

        cvBronzeCard.setAlpha(0.6f);
        cvSilverCard.setAlpha(0.6f);
        cvGoldCard.setAlpha(0.6f);
        cvDiamondCard.setAlpha(0.6f);
    }
}