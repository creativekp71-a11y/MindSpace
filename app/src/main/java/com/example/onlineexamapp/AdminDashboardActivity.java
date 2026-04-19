package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore fStore;
    private TextView tvTotalQuizzesCount, tvTotalUsersCount;
    private TextView tvNewUsersCount, tvTotalAttemptsCount;
    private SwipeRefreshLayout swipeRefreshDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#6C5CE7"));

        fStore = FirebaseFirestore.getInstance();

        tvTotalQuizzesCount = findViewById(R.id.tvTotalQuizzesCount);
        tvTotalUsersCount = findViewById(R.id.tvTotalUsersCount);
        tvNewUsersCount = findViewById(R.id.tvNewUsersCount);
        tvTotalAttemptsCount = findViewById(R.id.tvTotalAttemptsCount);
        swipeRefreshDashboard = findViewById(R.id.swipeRefreshDashboard);

        // Dashboard logic is now managed in setupCardClicks()

        loadStats();

        swipeRefreshDashboard.setOnRefreshListener(this::loadStats);

        // Toasts for unimplemented features
        View headerBg = findViewById(R.id.headerBg);
        if (headerBg != null) {
            headerBg.setOnClickListener(v -> {}); // Prevent clicks through header
        }
        
        setupCardClicks();
    }

    private void loadStats() {
        if (!swipeRefreshDashboard.isRefreshing()) {
            swipeRefreshDashboard.setRefreshing(true);
        }

        // Fetch User Count
        fStore.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                int count = task.getResult().size();
                tvTotalUsersCount.setText(String.valueOf(count));
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Unknown";
                Toast.makeText(this, "Users fetch error: " + error, Toast.LENGTH_SHORT).show();
            }
            checkRefreshComplete();
        });

        // Fetch Quizzes Count (DiscoveryActivities)
        fStore.collection("DiscoveryActivities").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                int count = task.getResult().size();
                tvTotalQuizzesCount.setText(String.valueOf(count));
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Unknown";
                Toast.makeText(this, "Quizzes fetch error: " + error, Toast.LENGTH_SHORT).show();
            }
            checkRefreshComplete();
        });

        // Fetch New Users (Last 24h)
        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        fStore.collection("Users")
                .whereGreaterThanOrEqualTo("registrationTimestamp", oneDayAgo)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                tvNewUsersCount.setText("+" + task.getResult().size());
            } else {
                tvNewUsersCount.setText("0");
            }
            checkRefreshComplete();
        });

        // Fetch Total Quiz Attempts
        fStore.collection("QuizAttempts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                tvTotalAttemptsCount.setText(String.valueOf(task.getResult().size()));
            } else {
                tvTotalAttemptsCount.setText("0");
            }
            checkRefreshComplete();
        });
    }

    private void setupCardClicks() {
        View ivLogout = findViewById(R.id.ivLogout);
        View cardUsers = findViewById(R.id.cardManageUsers);
        View cardQuizzes = findViewById(R.id.cardTotalQuizzes);
        View cardMembers = findViewById(R.id.cardTotalMembers);
        View cardAuthors = findViewById(R.id.cardManageAuthors);
        View cardHealthCenter = findViewById(R.id.cardHealthCenter);
        View cardBroadcast = findViewById(R.id.cardBroadcast);

        if (ivLogout != null) {
            ivLogout.setOnClickListener(v -> {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out from the Admin Center?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                            getSharedPreferences("auth_prefs", MODE_PRIVATE).edit().putBoolean("is_admin_logged_in", false).apply();
                            Intent intent = new Intent(this, SignInActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        if (cardUsers != null) {
            cardUsers.setOnClickListener(v -> startActivity(new Intent(this, AdminManageUsersActivity.class)));
        }

        if (cardQuizzes != null) {
            cardQuizzes.setOnClickListener(v -> startActivity(new Intent(this, AdminManageQuizzesActivity.class)));
        }

        if (cardHealthCenter != null) {
            cardHealthCenter.setOnClickListener(v -> startActivity(new Intent(this, AdminManageReportsActivity.class)));
        }

        if (cardAuthors != null) {
            cardAuthors.setOnClickListener(v -> startActivity(new Intent(this, AdminManageAuthorsActivity.class)));
        }

        if (cardMembers != null) {
            cardMembers.setOnClickListener(v -> startActivity(new Intent(this, AdminManageUsersActivity.class)));
        }

        if (cardBroadcast != null) {
            cardBroadcast.setOnClickListener(v -> startActivity(new Intent(this, AdminBroadcastActivity.class)));
        }

        View btnAnalytics = findViewById(R.id.btnViewFullAnalytics);
        if (btnAnalytics != null) {
            btnAnalytics.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminAnalyticsActivity.class));
            });
        }
        
        View cardEngagement = findViewById(R.id.cardEngagementInsights);
        if (cardEngagement != null) {
            cardEngagement.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminAnalyticsActivity.class));
            });
        }
    }

    private int completedTasks = 0;
    private void checkRefreshComplete() {
        completedTasks++;
        if (completedTasks >= 4) {
            completedTasks = 0;
            swipeRefreshDashboard.setRefreshing(false);
        }
    }
}
