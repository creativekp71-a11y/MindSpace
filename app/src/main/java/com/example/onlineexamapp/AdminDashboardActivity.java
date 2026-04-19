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

        ImageView ivLogout = findViewById(R.id.ivLogout);
        View cardManageUsers = findViewById(R.id.cardManageUsers);

        if (ivLogout != null) {
            ivLogout.setOnClickListener(v -> {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(AdminDashboardActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out from the Admin Center?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            // Sign out from Firebase
                            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                            // Clear Admin Persistence
                            getSharedPreferences("auth_prefs", MODE_PRIVATE)
                                .edit()
                                .putBoolean("is_admin_logged_in", false)
                                .apply();
                            // Return to Sign In
                            startActivity(new Intent(AdminDashboardActivity.this, SignInActivity.class));
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        if (cardManageUsers != null) {
            cardManageUsers.setOnClickListener(v -> {
                startActivity(new Intent(AdminDashboardActivity.this, AdminManageUsersActivity.class));
            });
        }

        loadStats();

        swipeRefreshDashboard.setOnRefreshListener(this::loadStats);

        // Toasts for unimplemented features
        View headerBg = findViewById(R.id.headerBg);
        if (headerBg != null) {
            headerBg.setOnClickListener(v -> {}); // Prevent clicks through header
        }
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
            }
            checkRefreshComplete();
        });
    }

    private int completedTasks = 0;
    private void checkRefreshComplete() {
        completedTasks++;
        if (completedTasks >= 2) {
            completedTasks = 0;
            swipeRefreshDashboard.setRefreshing(false);
        }
    }

    public void onBroadcastClick(View view) {
        Toast.makeText(this, "Broadcast feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    public void onReportsClick(View view) {
        Toast.makeText(this, "Reports feature coming soon!", Toast.LENGTH_SHORT).show();
    }
}
