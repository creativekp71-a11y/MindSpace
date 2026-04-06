package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // --- Fetch User Data for Greeting ---
        TextView tvUserGreeting = findViewById(R.id.tvUserGreeting);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            fStore.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String fullName = documentSnapshot.getString("full_name");
                    if (fullName != null && !fullName.isEmpty()) {
                        tvUserGreeting.setText("Hi, " + fullName + " 👋");
                    }
                }
            });
        }

        // 1. Niche Discover wale Icon ka connection
        View navDiscover = findViewById(R.id.navDiscover);
        if (navDiscover != null) {
            navDiscover.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, DiscoverActivity.class)));
        }

        // 2. View All wale Text ka connection
        TextView tvViewAllDiscover = findViewById(R.id.tvViewAllDiscover);
        if (tvViewAllDiscover != null) {
            tvViewAllDiscover.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, DiscoverActivity.class)));
        }

        // 3. Find Friends wale Banner ka connection
        View btnFindFriendsBanner = findViewById(R.id.btnFindFriendsBanner);
        if (btnFindFriendsBanner != null) {
            btnFindFriendsBanner.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, FindFriendsActivity.class)));
        }

        // ==========================================
        // 👉 Top Authors के 'View All' का कनेक्शन 👈
        // ==========================================
        android.widget.TextView tvViewAllAuthors = findViewById(R.id.tvViewAllAuthors);
        if (tvViewAllAuthors != null) {
            tvViewAllAuthors.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    android.content.Intent intent = new android.content.Intent(DashboardActivity.this, TopAuthorsActivity.class);
                    startActivity(intent);
                }
            });
        }

        // ==========================================
        // 👉 DASHBOARD CARD 1: Productivity Quiz 👈
        // ==========================================
        androidx.cardview.widget.CardView cardDash1 = findViewById(R.id.cardDashQuiz1); // अपनी XML वाली ID डालें
        if (cardDash1 != null) {
            cardDash1.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    android.content.Intent intent = new android.content.Intent(DashboardActivity.this, QuizActivity.class);
                    // 👉 ये लाइन सबसे ज़रूरी है
                    intent.putExtra("QUIZ_CATEGORY", "Productivity");
                    startActivity(intent);
                }
            });
        }

        // ==========================================
        // 👉 DASHBOARD CARD 2: Brilliant Minds 👈
        // ==========================================
        androidx.cardview.widget.CardView cardDash2 = findViewById(R.id.cardDashQuiz2); // अपनी XML वाली ID डालें
        if (cardDash2 != null) {
            cardDash2.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    android.content.Intent intent = new android.content.Intent(DashboardActivity.this, QuizActivity.class);
                    // 👉 यहाँ Brilliant Minds सेट किया
                    intent.putExtra("QUIZ_CATEGORY", "Brilliant Minds");
                    startActivity(intent);
                }
            });
        }

        // ==========================================
        // 👉 DASHBOARD CARD 3: Having Fun 👈
        // ==========================================
        androidx.cardview.widget.CardView cardDash3 = findViewById(R.id.cardDashQuiz3); // अपनी XML वाली ID डालें
        if (cardDash3 != null) {
            cardDash3.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    android.content.Intent intent = new android.content.Intent(DashboardActivity.this, QuizActivity.class);
                    // 👉 यहाँ Having Fun सेट किया
                    intent.putExtra("QUIZ_CATEGORY", "Having Fun");
                    startActivity(intent);
                }
            });
        }

        // ==========================================
        // 🔍 Top Search Icon का कनेक्शन
        // ==========================================

        // 🚨 ध्यान दें: R.id.ivSearch की जगह अपने डिज़ाइन (XML) वाले सर्च आइकॉन की सही ID डालना
        android.widget.ImageView iconSearch = findViewById(R.id.ivSearch);

        if (iconSearch != null) {
            iconSearch.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {

                    // पुराना Toast मैसेज हटा दिया!
                    // अब सीधा नया Search Page खुलेगा 🚀
                    android.content.Intent intent = new android.content.Intent(DashboardActivity.this, SearchActivity.class);
                    startActivity(intent);

                }
            });
        }

        // ==========================================
        // 👉 Notification आइकन का कनेक्शन 🔔 👈
        // ==========================================
        android.widget.ImageView ivNotification = findViewById(R.id.ivBell); // 👈 यहाँ ivBell कर दिया
        if (ivNotification != null) {
            ivNotification.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    android.widget.Toast.makeText(DashboardActivity.this, "You have 0 new notifications 🔔", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }

        // ==========================================
        // 👉 Center Logo (Quick Play) का कनेक्शन 💡 👈
        // ==========================================
        android.widget.ImageView ivCenterLogo = findViewById(R.id.ivCenterLogo); // अपनी XML वाली ID यहाँ डालना
        if (ivCenterLogo != null) {
            ivCenterLogo.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    android.content.Intent intent = new android.content.Intent(DashboardActivity.this, QuizActivity.class);
                    // 👉 पार्सल में "Quick Play" नाम भेजा
                    intent.putExtra("QUIZ_CATEGORY", "Quick Play");
                    startActivity(intent);
                }
            });
        }

        // ==========================================
        // 👉 Leaderboard (Rank) वाले बटन का कनेक्शन 👈
        // ==========================================
        android.view.View navLeaderboard = findViewById(R.id.navLeaderboard); // XML वाली ID
        if (navLeaderboard != null) {
            navLeaderboard.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    startActivity(new android.content.Intent(DashboardActivity.this, LeaderboardActivity.class));
                }
            });
        }

        // ==========================================
        // 👉 Profile वाले बटन का कनेक्शन 👈
        // ==========================================
        android.view.View navProfile = findViewById(R.id.navProfile); // XML वाली ID चेक कर लेना
        if (navProfile != null) {
            navProfile.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    startActivity(new android.content.Intent(DashboardActivity.this, ProfileActivity.class));
                }
            });
        }
    }
}