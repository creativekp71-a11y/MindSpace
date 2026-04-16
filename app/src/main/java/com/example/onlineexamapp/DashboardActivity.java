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
import androidx.recyclerview.widget.LinearLayoutManager; // 🌟 Horizontal Scroll के लिए नया Import
import com.bumptech.glide.Glide;
import android.util.Base64;
import android.widget.ImageView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView rvHomeDiscover, rvHomeAuthors;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListenerRegistration badgeListener;
    private int activeTasks = 0;

    // 🌟 बदलाव 1: यहाँ DiscoveryAdapter की जगह DashboardAdapter कर दिया
    private DashboardAdapter dashboardAdapter;

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

        // 🌟 बदलाव 2: Horizontal Scroll for Discoveries (as requested)
        rvHomeDiscover.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHomeDiscover.setHasFixedSize(true);

        discoveryList = new ArrayList<>();

        // 🌟 बदलाव 3: यहाँ नया वाला DashboardAdapter सेट कर दिया है
        dashboardAdapter = new DashboardAdapter(this, discoveryList);
        rvHomeDiscover.setAdapter(dashboardAdapter);

        rvHomeAuthors = findViewById(R.id.rvHomeAuthors);
        rvHomeAuthors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHomeAuthors.setHasFixedSize(true);
        authorList = new ArrayList<>();
        authorAdapter = new AuthorHomeAdapter(this, authorList);
        rvHomeAuthors.setAdapter(authorAdapter);



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
                            Glide.with(DashboardActivity.this)
                                    .load(imageBytes)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_user_placeholder)
                                    .into(ivHeaderProfile);
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

        // All navigation click listeners are now consolidated in setupBottomNavigation()

        // Quick Play logic moved to setupBottomNavigation()

        // --- Bottom Navigation Setup ---
        setupBottomNavigation();

        // --- SwipeRefreshLayout Setup ---
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(R.color.purple_500);
            swipeRefreshLayout.setOnRefreshListener(() -> refreshDashboard());
        }

        // --- Initial Load ---
        refreshDashboard();
    }

    private void refreshDashboard() {
        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
        
        activeTasks = 2; // fetchDiscoveries and fetchTopAuthors
        fetchDiscoveries();
        fetchTopAuthors();
        setupNotificationBadge(); 
    }

    private synchronized void checkRefreshFinished() {
        activeTasks--;
        if (activeTasks <= 0 && swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void setupNotificationBadge() {
        if (badgeListener != null) {
            badgeListener.remove();
        }

        View viewBellBadge = findViewById(R.id.viewBellBadge);
        if (viewBellBadge == null) return;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            viewBellBadge.setVisibility(View.GONE);
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        // Use a snapshot listener that stays active
        badgeListener = fStore.collection("Notifications").document(uid)
                .collection("UserNotifications")
                .whereEqualTo("read", false)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        viewBellBadge.setVisibility(value.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                });
    }

    private void setupBottomNavigation() {
        // --- Core Navigation Items ---
        findViewById(R.id.navHome).setOnClickListener(v -> {
            // Already on Home
        });

        findViewById(R.id.navDiscover).setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, DiscoverActivity.class));
        });

        findViewById(R.id.navCreate).setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, AddDiscoveryActivity.class));
        });

        findViewById(R.id.navLeaderboard).setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, LeaderboardActivity.class));
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
        });

        // --- Additional Dashboard Actions ---
        View btnFindFriendsBanner = findViewById(R.id.btnFindFriendsBanner);
        if (btnFindFriendsBanner != null) {
            btnFindFriendsBanner.setOnClickListener(v -> 
                startActivity(new Intent(DashboardActivity.this, FindFriendsActivity.class))
            );
        }

        findViewById(R.id.tvViewAllDiscover).setOnClickListener(v -> 
            startActivity(new Intent(DashboardActivity.this, DiscoverActivity.class))
        );

        findViewById(R.id.tvViewAllAuthors).setOnClickListener(v -> 
            startActivity(new Intent(DashboardActivity.this, TopAuthorsActivity.class))
        );

        findViewById(R.id.ivSearch).setOnClickListener(v -> 
            startActivity(new Intent(DashboardActivity.this, SearchActivity.class))
        );

        findViewById(R.id.ivBell).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MainHomeActivity.class);
            intent.putExtra("EXTRA_OPEN_TAB", "TAB_NOTIFICATIONS"); // Simplified reference
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (badgeListener != null) {
            badgeListener.remove();
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
                    // 🌟 बदलाव 4: यहाँ भी dashboardAdapter कर दिया है
                    dashboardAdapter.notifyDataSetChanged();
                    checkRefreshFinished();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching discoveries: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    checkRefreshFinished();
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
                    checkRefreshFinished();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching authors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    checkRefreshFinished();
                });
    }
}
