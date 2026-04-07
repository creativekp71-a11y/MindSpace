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
import com.google.firebase.firestore.Query;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import android.util.Base64;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView rvHomeDiscover, rvHomeAuthors;
    private DiscoveryAdapter discoveryAdapter;
    private AuthorHomeAdapter authorAdapter;
    private List<DiscoveryActivityModel> discoveryList;
    private List<UserModel> authorList;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // --- Fetch User Data for Greeting ---
        TextView tvUserGreeting = findViewById(R.id.tvUserGreeting);
        fStore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        rvHomeDiscover = findViewById(R.id.rvHomeDiscover);
        discoveryList = new ArrayList<>();
        discoveryAdapter = new DiscoveryAdapter(this, discoveryList, true);
        rvHomeDiscover.setAdapter(discoveryAdapter);

        rvHomeAuthors = findViewById(R.id.rvHomeAuthors);
        authorList = new ArrayList<>();
        authorAdapter = new AuthorHomeAdapter(this, authorList);
        rvHomeAuthors.setAdapter(authorAdapter);

        fetchDiscoveries();
        fetchTopAuthors();

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            ImageView ivHeaderProfile = findViewById(R.id.ivHeaderProfilePic);

            fStore.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String fullName = documentSnapshot.getString("full_name");
                    String profilePic = documentSnapshot.getString("profile_pic");

                    if (fullName != null && !fullName.isEmpty()) {
                        tvUserGreeting.setText("Hi, " + fullName + " 👋");
                    }

                    if (profilePic != null && !profilePic.isEmpty()) {
                        try {
                            byte[] imageBytes = Base64.decode(profilePic, Base64.DEFAULT);
                            Glide.with(this).load(imageBytes).placeholder(R.drawable.ic_user_placeholder).into(ivHeaderProfile);
                        } catch (Exception e) {
                            ivHeaderProfile.setImageResource(R.drawable.ic_user_placeholder);
                        }
                    }
                }
            });

            // Click listener for header profile to jump to profile page
            if (ivHeaderProfile != null) {
                ivHeaderProfile.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, ProfileActivity.class)));
            }
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

        // Discovery items are now handled by rvHomeDiscover and DiscoveryAdapter


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

    private void fetchDiscoveries() {
        fStore.collection("DiscoveryActivities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    discoveryList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DiscoveryActivityModel model = document.toObject(DiscoveryActivityModel.class);
                        model.setId(document.getId());
                        discoveryList.add(model);
                    }
                    discoveryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching discoveries: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchTopAuthors() {
        fStore.collection("Users")
                .whereEqualTo("isAuthor", true)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    authorList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserModel author = document.toObject(UserModel.class);
                        author.setId(document.getId());
                        authorList.add(author);
                    }
                    authorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching authors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}