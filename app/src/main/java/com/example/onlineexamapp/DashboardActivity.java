package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView rvHomeDiscover, rvHomeAuthors;
    private SwipeRefreshLayout swipeRefreshLayout;

    private DashboardAdapter dashboardAdapter;
    private AuthorHomeAdapter authorAdapter;

    private final List<DiscoveryActivityModel> discoveryList = new ArrayList<>();
    private final List<UserModel> authorList = new ArrayList<>();

    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;

    private TextView tvUserGreeting;
    private ImageView ivHeaderProfile;

    private ListenerRegistration badgeListener;
    private ListenerRegistration chatBadgeListener;
    private int activeTasks = 0;
    private boolean isRefreshing = false;
    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupRecyclerViews();
        setupTopActions();
        setupBottomNavigation();
        setupSwipeRefresh();
        setupNotificationBadge();
    }

    private void initViews() {
        tvUserGreeting = findViewById(R.id.tvUserGreeting);
        ivHeaderProfile = findViewById(R.id.ivHeaderProfilePic);

        rvHomeDiscover = findViewById(R.id.rvHomeDiscover);
        rvHomeAuthors = findViewById(R.id.rvHomeAuthors);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void setupRecyclerViews() {
        if (rvHomeDiscover != null) {
            rvHomeDiscover.setLayoutManager(
                    new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            );
            rvHomeDiscover.setHasFixedSize(true);
            rvHomeDiscover.setNestedScrollingEnabled(false);

            dashboardAdapter = new DashboardAdapter(this, discoveryList);
            rvHomeDiscover.setAdapter(dashboardAdapter);
        }

        if (rvHomeAuthors != null) {
            rvHomeAuthors.setLayoutManager(
                    new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            );
            rvHomeAuthors.setHasFixedSize(true);
            rvHomeAuthors.setNestedScrollingEnabled(false);

            authorAdapter = new AuthorHomeAdapter(this, authorList);
            rvHomeAuthors.setAdapter(authorAdapter);
        }
    }

    private void setupTopActions() {
        if (ivHeaderProfile != null) {
            ivHeaderProfile.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, ProfileActivity.class)));
        }

        View tvViewAllDiscover = findViewById(R.id.tvViewAllDiscover);
        if (tvViewAllDiscover != null) {
            tvViewAllDiscover.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, DiscoverActivity.class)));
        }

        View btnFindFriendsBanner = findViewById(R.id.btnFindFriendsBanner);
        if (btnFindFriendsBanner != null) {
            btnFindFriendsBanner.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, FindFriendsActivity.class)));
        }

        View tvViewAllAuthors = findViewById(R.id.tvViewAllAuthors);
        if (tvViewAllAuthors != null) {
            tvViewAllAuthors.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, TopAuthorsActivity.class)));
        }

        View ivSearch = findViewById(R.id.ivSearch);
        if (ivSearch != null) {
            ivSearch.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, SearchActivity.class)));
        }

        View ivChat = findViewById(R.id.ivChat);
        if (ivChat != null) {
            ivChat.setOnClickListener(v -> {
                startActivity(new Intent(DashboardActivity.this, MessagesListActivity.class));
            });
        }

        setupChatBadge();
    }

    private void setupBottomNavigation() {
        View navHome = findViewById(R.id.navHome);
        View navDiscover = findViewById(R.id.navDiscover);
        View navCreate = findViewById(R.id.navCreate);
        View navLeaderboard = findViewById(R.id.navLeaderboard);
        View navProfile = findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // Already on Home
            });
        }

        if (navDiscover != null) {
            navDiscover.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, DiscoverActivity.class)));
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, AddDiscoveryActivity.class)));
        }

        if (navLeaderboard != null) {
            navLeaderboard.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, LeaderboardActivity.class)));
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, ProfileActivity.class)));
        }
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(R.color.purple_500);
            swipeRefreshLayout.setOnRefreshListener(this::refreshDashboard);
        }
    }

    private void refreshDashboard() {
        if (isRefreshing) return;
        isRefreshing = true;

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.post(() -> {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }

        // Reset tasks counter (User profile, Discoveries, Top Authors)
        activeTasks = 3;

        fetchUserProfile();
        fetchDiscoveries();
        fetchTopAuthors();
    }

    private synchronized void checkRefreshFinished() {
        activeTasks--;

        if (activeTasks <= 0 && swipeRefreshLayout != null) {
            activeTasks = 0; // Prevent negative values
            isRefreshing = false;
            swipeRefreshLayout.post(() -> {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    private void fetchUserProfile() {
        if (mAuth.getCurrentUser() == null) {
            checkRefreshFinished();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        fStore.collection("Users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("full_name");
                        String profilePic = documentSnapshot.getString("profile_pic");

                        if (tvUserGreeting != null && fullName != null && !fullName.isEmpty()) {
                            tvUserGreeting.setText("Hi, " + fullName + " 👋");
                        }

                        if (ivHeaderProfile != null) {
                            if (profilePic != null && !profilePic.isEmpty()) {
                                try {
                                    byte[] imageBytes = Base64.decode(profilePic, Base64.DEFAULT);
                                    Glide.with(DashboardActivity.this)
                                            .load(imageBytes)
                                            .circleCrop()
                                            .placeholder(R.drawable.ic_user_placeholder)
                                            .error(R.drawable.ic_user_placeholder)
                                            .into(ivHeaderProfile);
                                } catch (Exception e) {
                                    ivHeaderProfile.setImageResource(R.drawable.ic_user_placeholder);
                                }
                            } else {
                                ivHeaderProfile.setImageResource(R.drawable.ic_user_placeholder);
                            }
                        }
                    }

                    checkRefreshFinished();
                })
                .addOnFailureListener(e -> checkRefreshFinished());
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

                    if (dashboardAdapter != null) {
                        dashboardAdapter.notifyDataSetChanged();
                    }

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

                    if (authorAdapter != null) {
                        authorAdapter.notifyDataSetChanged();
                    }

                    checkRefreshFinished();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching authors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    checkRefreshFinished();
                });
    }

    private void setupNotificationBadge() {
        if (badgeListener != null) {
            badgeListener.remove();
        }

        View viewBellBadge = findViewById(R.id.viewBellBadge);
        if (viewBellBadge == null) return;

        if (mAuth.getCurrentUser() == null) {
            viewBellBadge.setVisibility(View.GONE);
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        badgeListener = fStore.collection("Notifications")
                .document(uid)
                .collection("UserNotifications")
                .whereEqualTo("read", false)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        viewBellBadge.setVisibility(value.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                });
    }

    private void setupChatBadge() {
        if (chatBadgeListener != null) {
            chatBadgeListener.remove();
        }

        View viewChatBadge = findViewById(R.id.viewChatBadge);
        if (viewChatBadge == null) return;

        if (mAuth.getCurrentUser() == null) {
            viewChatBadge.setVisibility(View.GONE);
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        chatBadgeListener = fStore.collection("Conversations")
                .whereArrayContains("participants", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    boolean hasUnread = false;
                    if (value != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                            Map<String, Object> unreadMap = (Map<String, Object>) doc.get("unreadCount");
                            if (unreadMap != null && unreadMap.containsKey(uid)) {
                                Long count = (Long) unreadMap.get(uid);
                                if (count != null && count > 0) {
                                    hasUnread = true;
                                    break;
                                }
                            }
                        }
                        viewChatBadge.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstLoad) {
            // Respect the rule: don't call it immediately if it's the first time
            // Wait for the SwipeRefreshLayout to be fully measured and ready
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.post(() -> {
                    if (isFirstLoad) {
                        isFirstLoad = false;
                        refreshDashboard();
                    }
                });
            }
        } else {
            // For subsequent returns (like coming back from Notifications), refresh directly
            refreshDashboard();
        }

        // Refresh badge listeners on resume to ensure update
        setupNotificationBadge();
        setupChatBadge();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (badgeListener != null) {
            badgeListener.remove();
        }
        if (chatBadgeListener != null) {
            chatBadgeListener.remove();
        }
    }
}