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
import com.google.firebase.firestore.ListenerRegistration;

public class AdminDashboardActivity extends BaseActivity {

    private FirebaseFirestore fStore;
    private TextView tvTotalQuizzesCount, tvTotalUsersCount;
    private TextView tvNewUsersCount, tvTotalAttemptsCount;
    private TextView tvWelcomeBack, tvLiveTime;
    private SwipeRefreshLayout swipeRefreshDashboard;
    private com.google.firebase.firestore.ListenerRegistration usersListener, quizzesListener, attemptsListener, newUsersListener;
    private android.os.Handler timeHandler = new android.os.Handler();
    private Runnable timeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        // Immersive Status Bar Fix
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        fStore = FirebaseFirestore.getInstance();

        tvTotalQuizzesCount = findViewById(R.id.tvTotalQuizzesCount);
        tvTotalUsersCount = findViewById(R.id.tvTotalUsersCount);
        tvNewUsersCount = findViewById(R.id.tvNewUsersCount);
        tvTotalAttemptsCount = findViewById(R.id.tvTotalAttemptsCount);
        tvWelcomeBack = findViewById(R.id.tvWelcomeBack);
        tvLiveTime = findViewById(R.id.tvLiveTime);
        swipeRefreshDashboard = findViewById(R.id.swipeRefreshDashboard);

        setupDynamicHeader();
        loadStats();

        swipeRefreshDashboard.setOnRefreshListener(() -> {
            // Manual check for any missing live updates
            loadStats();
        });

        // Toasts for unimplemented features
        View headerBg = findViewById(R.id.headerBg);
        if (headerBg != null) {
            headerBg.setOnClickListener(v -> {}); // Prevent clicks through header
        }
        
        setupCardClicks();
    }

    private void setupDynamicHeader() {
        // 1. Dynamic Greeting
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 12) greeting = "Good Morning,";
        else if (hour >= 12 && hour < 17) greeting = "Good Afternoon,";
        else if (hour >= 17 && hour < 21) greeting = "Good Evening,";
        else greeting = "Good Night,";
        tvWelcomeBack.setText(greeting);

        // 2. Real-time Clock
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE, dd MMM • hh:mm a", java.util.Locale.getDefault());
                tvLiveTime.setText(sdf.format(new java.util.Date()));
                timeHandler.postDelayed(this, 10000); // Update every 10 seconds for smoothness
            }
        };
        timeHandler.post(timeRunnable);
    }

    private void loadStats() {
        if (usersListener != null) usersListener.remove();
        if (quizzesListener != null) quizzesListener.remove();
        if (attemptsListener != null) attemptsListener.remove();
        if (newUsersListener != null) newUsersListener.remove();

        // 1. Live Users Count (Excluding System Admin)
        usersListener = fStore.collection("Users").addSnapshotListener((snapshot, e) -> {
            if (snapshot != null) {
                int realUserCount = 0;
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                    String email = doc.getString("email");
                    if (email != null && !email.equalsIgnoreCase("admin@mindspace.com")) {
                        realUserCount++;
                    }
                }
                tvTotalUsersCount.setText(String.valueOf(realUserCount));
            }
            swipeRefreshDashboard.setRefreshing(false);
        });

        // 2. Live Quizzes Count
        quizzesListener = fStore.collection("DiscoveryActivities").addSnapshotListener((snapshot, e) -> {
            if (snapshot != null) tvTotalQuizzesCount.setText(String.valueOf(snapshot.size()));
            swipeRefreshDashboard.setRefreshing(false);
        });

        // 3. Live Attempts Count
        attemptsListener = fStore.collection("QuizAttempts").addSnapshotListener((snapshot, e) -> {
            if (snapshot != null) tvTotalAttemptsCount.setText(String.valueOf(snapshot.size()));
            swipeRefreshDashboard.setRefreshing(false);
        });

        // 4. Live New Users (Last 24h) (Excluding System Admin)
        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        newUsersListener = fStore.collection("Users")
                .whereGreaterThanOrEqualTo("registrationTimestamp", oneDayAgo)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot != null) {
                        int realNewUserCount = 0;
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                            String email = doc.getString("email");
                            if (email != null && !email.equalsIgnoreCase("admin@mindspace.com")) {
                                realNewUserCount++;
                            }
                        }
                        tvNewUsersCount.setText("+" + realNewUserCount);
                    }
                    swipeRefreshDashboard.setRefreshing(false);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeHandler != null && timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
        if (usersListener != null) usersListener.remove();
        if (quizzesListener != null) quizzesListener.remove();
        if (attemptsListener != null) attemptsListener.remove();
        if (newUsersListener != null) newUsersListener.remove();
    }

    private void setupCardClicks() {
        View ivLogout = findViewById(R.id.ivLogout);
        View cardUsers = findViewById(R.id.cardManageUsers);
        View cardQuizzes = findViewById(R.id.cardTotalQuizzes);
        View cardMembers = findViewById(R.id.cardTotalMembers);
        View cardAuthors = findViewById(R.id.cardManageAuthors);
        View cardNewUsers = findViewById(R.id.cardNewUsers);
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

        if (cardNewUsers != null) {
            cardNewUsers.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminManageUsersActivity.class);
                intent.putExtra("FILTER_TYPE", "last_24h");
                startActivity(intent);
            });
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
