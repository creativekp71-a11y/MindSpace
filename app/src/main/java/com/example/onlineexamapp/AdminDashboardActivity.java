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
    private TextView tvTotalQuizzesCount, tvVerifiedQuizzesCount, tvPendingAppsCount, tvTotalUsersCount;
    private SwipeRefreshLayout swipeRefreshDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        fStore = FirebaseFirestore.getInstance();

        tvTotalQuizzesCount = findViewById(R.id.tvTotalQuizzesCount);
        tvVerifiedQuizzesCount = findViewById(R.id.tvVerifiedQuizzesCount);
        tvPendingAppsCount = findViewById(R.id.tvPendingAppsCount);
        tvTotalUsersCount = findViewById(R.id.tvTotalUsersCount);
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
                
                // MOCK Verified/Pending until a status field is added
                tvVerifiedQuizzesCount.setText(String.valueOf((int)(count * 0.8)));
                tvPendingAppsCount.setText(String.valueOf((int)(count * 0.2)));
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Unknown";
                Toast.makeText(this, "Quizzes fetch error: " + error, Toast.LENGTH_SHORT).show();
            }
            checkRefreshComplete();
        });
    }

    private void setupCardClicks() {
        View ivLogout = findViewById(R.id.ivLogout);
        View cardUsers = findViewById(R.id.cardManageUsers);
        View cardQuizzes = findViewById(R.id.cardTotalQuizzes);
        View cardVerified = findViewById(R.id.cardVerifiedQuizzes);
        View cardPending = findViewById(R.id.cardPendingReview);
        View cardMembers = findViewById(R.id.cardTotalMembers);
        View cardReports = findViewById(R.id.cardHealthReports);
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

        if (cardVerified != null) {
            cardVerified.setOnClickListener(v -> Toast.makeText(this, "Verified Content List - Coming Soon!", Toast.LENGTH_SHORT).show());
        }

        if (cardPending != null) {
            cardPending.setOnClickListener(v -> Toast.makeText(this, "Content Review Queue - Coming Soon!", Toast.LENGTH_SHORT).show());
        }

        if (cardReports != null) {
            cardReports.setOnClickListener(v -> startActivity(new Intent(this, AdminManageAuthorsActivity.class)));
        }

        if (cardBroadcast != null) {
            cardBroadcast.setOnClickListener(v -> Toast.makeText(this, "Broadcast feature coming soon!", Toast.LENGTH_SHORT).show());
        }

        if (cardMembers != null) {
            cardMembers.setOnClickListener(v -> startActivity(new Intent(this, AdminManageUsersActivity.class)));
        }
    }

    private int completedTasks = 0;
    private void checkRefreshComplete() {
        completedTasks++;
        if (completedTasks >= 2) {
            completedTasks = 0;
            swipeRefreshDashboard.setRefreshing(false);
        }
    }
}
