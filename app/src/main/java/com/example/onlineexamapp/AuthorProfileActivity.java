package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthorProfileActivity extends AppCompatActivity {

    private String authorUid, currentUserId;
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;

    private ImageView ivCover, ivAvatar;
    private TextView tvFullName, tvUsername, tvBio;
    private TextView tvFollowers, tvFollowing, tvDiscoveries, tvPoints, tvRank, tvReportUser;
    private TextView tvMessageHint;
    private AppCompatButton btnFollow, btnMessage;
    private RecyclerView rvDiscoveries;
    private DiscoveryAdapter adapter;
    private List<DiscoveryActivityModel> discoveryList;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_profile);

        authorUid = getIntent().getStringExtra("authorUid");
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getUid();

        if (authorUid == null) {
            Toast.makeText(this, "Author not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initUI();
        loadAuthorDetails();
        loadAuthorDiscoveries();
        if (currentUserId != null) {
            checkFollowStatus();
        }

        btnFollow.setOnClickListener(v -> toggleFollow());
        btnMessage.setOnClickListener(v -> openChat());

        // ✅ Social Stats Click listeners
        findViewById(R.id.llAuthorFollowers).setOnClickListener(v -> {
            Intent intent = new Intent(this, SocialListActivity.class);
            intent.putExtra("userId", authorUid);
            intent.putExtra("type", "followers");
            startActivity(intent);
        });

        findViewById(R.id.llAuthorFollowing).setOnClickListener(v -> {
            Intent intent = new Intent(this, SocialListActivity.class);
            intent.putExtra("userId", authorUid);
            intent.putExtra("type", "following");
            startActivity(intent);
        });

        findViewById(R.id.llAuthorDiscoveries).setOnClickListener(v -> {
            Intent intent = new Intent(this, UserDiscoveriesActivity.class);
            intent.putExtra("userId", authorUid);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        if (authorUid != null && !authorUid.equals(currentUserId)) {
            android.view.MenuItem reportItem = menu.add(0, 101, 0, "Report User")
                .setIcon(R.drawable.ic_notification);
            reportItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_NEVER);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                reportItem.setTooltipText("Report this user for violations");
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == 101) {
            showUserReportDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUserReportDialog() {
        String[] reasons = {"Inappropriate Profile", "Spam Account", "Harassment", "Fake Identity", "Other"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Report this User")
                .setItems(reasons, (dialog, which) -> {
                    submitUserReport(reasons[which]);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitUserReport(String reason) {
        if (currentUserId == null) return;

        fStore.collection("Users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
            String reporterName = userDoc.getString("full_name");
            ReportModel report = new ReportModel(
                    currentUserId,
                    reporterName != null ? reporterName : "Anonymous",
                    authorUid,
                    tvFullName.getText().toString(),
                    "user",
                    reason,
                    "Reported from Profile Screen"
            );

            fStore.collection("Reports")
                    .add(report)
                    .addOnSuccessListener(docRef -> Toast.makeText(this, "User reported. Admin will review this shortly.", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void initUI() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ivCover = findViewById(R.id.ivAuthorCover);
        ivAvatar = findViewById(R.id.ivAuthorAvatar);
        tvFullName = findViewById(R.id.tvAuthorFullName);
        tvUsername = findViewById(R.id.tvAuthorUsername);
        tvBio = findViewById(R.id.tvAuthorBio);
        tvFollowers = findViewById(R.id.tvFollowersCount);
        tvFollowing = findViewById(R.id.tvFollowingCount);
        tvDiscoveries = findViewById(R.id.tvDiscoveryCount);
        tvPoints = findViewById(R.id.tvAuthorPoints);
        tvRank = findViewById(R.id.tvAuthorRank);
        btnFollow = findViewById(R.id.btnAuthorFollow);
        btnMessage = findViewById(R.id.btnAuthorMessage);
        tvMessageHint = findViewById(R.id.tvMessagePermissionHint);
        tvReportUser = findViewById(R.id.tvReportUser);

        tvReportUser.setOnClickListener(v -> showUserReportDialog());
        TooltipCompat.setTooltipText(tvReportUser, "Report this user for violations");

        rvDiscoveries = findViewById(R.id.rvAuthorDiscoveries);
        rvDiscoveries.setLayoutManager(new LinearLayoutManager(this));
        discoveryList = new ArrayList<>();
        adapter = new DiscoveryAdapter(this, discoveryList);
        rvDiscoveries.setAdapter(adapter);

        // Stealth Admin: Hide follow button for admin account
        android.content.SharedPreferences prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        boolean isAdmin = prefs.getBoolean("is_admin_logged_in", false);
        
        if (authorUid.equals(currentUserId)) {
            btnFollow.setVisibility(View.GONE);
            tvReportUser.setVisibility(View.GONE);
        }
    }

    private void loadAuthorDetails() {
        fStore.collection("Users").document(authorUid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String fullName = doc.getString("full_name");
                String username = doc.getString("username");
                String bio = doc.getString("bio");
                String profilePic = doc.getString("profile_pic");
                String coverPic = doc.getString("cover_pic");
                Long followers = doc.getLong("followersCount");
                Long following = doc.getLong("followingCount");
                Long points = doc.getLong("points");
                String rank = doc.getString("rank");

                tvFullName.setText(fullName != null ? fullName : "Unknown Author");
                tvUsername.setText(username != null ? "@" + username : "");
                tvBio.setText(bio != null && !bio.isEmpty() ? bio : "No bio available.");
                tvFollowers.setText(String.valueOf(followers != null ? followers : 0));
                tvFollowing.setText(String.valueOf(following != null ? following : 0));
                long safePoints = Math.max(0, points != null ? points : 0);
                tvPoints.setText(String.valueOf(safePoints));
                
                // Fetch real-time rank based on points
                if (points != null) {
                    fStore.collection("Users")
                            .whereGreaterThan("points", Math.max(0, points != null ? points : 0))
                            .get()
                            .addOnSuccessListener(snap -> {
                                int higherNonAdminCount = 0;
                                for (com.google.firebase.firestore.DocumentSnapshot d : snap.getDocuments()) {
                                    String email = d.getString("email");
                                    String name = d.getString("full_name");
                                    Boolean isAdmin = d.getBoolean("isAdmin");
                                    
                                    if ("admin@mindspace.com".equalsIgnoreCase(email) 
                                        || "System Admin".equalsIgnoreCase(name)
                                        || (isAdmin != null && isAdmin)) {
                                        continue;
                                    }
                                    higherNonAdminCount++;
                                }
                                tvRank.setText(String.valueOf(higherNonAdminCount + 1));
                            });
                } else {
                    tvRank.setText("--");
                }

                if (profilePic != null && !profilePic.isEmpty()) {
                    try {
                        byte[] bytes = Base64.decode(profilePic, Base64.DEFAULT);
                        Glide.with(this).load(bytes).placeholder(R.drawable.ic_user_placeholder).into(ivAvatar);
                    } catch (Exception e) {
                        ivAvatar.setImageResource(R.drawable.ic_user_placeholder);
                    }
                }

                if (coverPic != null && !coverPic.isEmpty()) {
                    try {
                        byte[] bytes = Base64.decode(coverPic, Base64.DEFAULT);
                        Glide.with(this).load(bytes).placeholder(R.drawable.cover_photo).into(ivCover);
                    } catch (Exception e) {
                        ivCover.setImageResource(R.drawable.cover_photo);
                    }
                }
            }
        });
    }

    private void loadAuthorDiscoveries() {
        fStore.collection("DiscoveryActivities")
                .whereEqualTo("authorId", authorUid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    discoveryList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        DiscoveryActivityModel model = doc.toObject(DiscoveryActivityModel.class);
                        if (model != null) {
                            model.setId(doc.getId());
                            discoveryList.add(model);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    tvDiscoveries.setText(String.valueOf(discoveryList.size()));
                }).addOnFailureListener(e -> {
                    // Fail silently or handle order-by index issue
                    fStore.collection("DiscoveryActivities")
                        .whereEqualTo("authorId", authorUid)
                        .get()
                        .addOnSuccessListener(snaps -> {
                            discoveryList.clear();
                            for (com.google.firebase.firestore.DocumentSnapshot doc : snaps) {
                                DiscoveryActivityModel model = doc.toObject(DiscoveryActivityModel.class);
                                if (model != null) {
                                    model.setId(doc.getId());
                                    discoveryList.add(model);
                                }
                            }
                            adapter.notifyDataSetChanged();
                            tvDiscoveries.setText(String.valueOf(discoveryList.size()));
                        });
                });
    }

    private void checkFollowStatus() {
        fStore.collection("Following").document(currentUserId)
                .collection("UserFollowing").document(authorUid)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        btnFollow.setText("Unfollow");
                        btnFollow.setBackgroundResource(R.drawable.bg_btn_unfollow);
                        // Once we know we follow them, check if they follow us back for messaging
                        checkMutualFollowStatus();
                    } else {
                        btnFollow.setText("Follow");
                        btnFollow.setBackgroundResource(R.drawable.bg_btn_follow);
                        btnMessage.setVisibility(View.GONE);
                        tvMessageHint.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void checkMutualFollowStatus() {
        if (authorUid.equals(currentUserId)) {
            btnMessage.setVisibility(View.GONE);
            tvMessageHint.setVisibility(View.GONE);
            return;
        }

        // They follow us check
        fStore.collection("Following").document(authorUid)
                .collection("UserFollowing").document(currentUserId)
                .get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        btnMessage.setVisibility(View.VISIBLE);
                        tvMessageHint.setVisibility(View.GONE);
                    } else {
                        btnMessage.setVisibility(View.GONE);
                        tvMessageHint.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(e -> {
                    btnMessage.setVisibility(View.GONE);
                    tvMessageHint.setVisibility(View.VISIBLE);
                });
    }

    private void openChat() {
        String chatId;
        if (currentUserId.compareTo(authorUid) < 0) {
            chatId = currentUserId + "_" + authorUid;
        } else {
            chatId = authorUid + "_" + currentUserId;
        }

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("receiverId", authorUid);
        intent.putExtra("receiverName", tvFullName.getText().toString());
        startActivity(intent);
    }

    private void toggleFollow() {
        if (currentUserId == null) {
            Toast.makeText(this, "Please sign in to follow", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCurrentlyFollowing = btnFollow.getText().toString().equals("Unfollow");
        btnFollow.setEnabled(false);

        WriteBatch batch = fStore.batch();
        DocumentReference followingRef = fStore.collection("Following").document(currentUserId)
                .collection("UserFollowing").document(authorUid);
        DocumentReference followersRef = fStore.collection("Followers").document(authorUid)
                .collection("UserFollowers").document(currentUserId);
        
        DocumentReference currentUserRef = fStore.collection("Users").document(currentUserId);
        DocumentReference targetAuthorRef = fStore.collection("Users").document(authorUid);

        if (isCurrentlyFollowing) {
            batch.delete(followingRef);
            batch.delete(followersRef);
            batch.update(currentUserRef, "followingCount", FieldValue.increment(-1));
            batch.update(targetAuthorRef, "followersCount", FieldValue.increment(-1));

            batch.commit().addOnSuccessListener(aVoid -> {
                btnFollow.setEnabled(true);
                btnFollow.setText("Follow");
                btnFollow.setBackgroundResource(R.drawable.bg_btn_follow);
                checkFollowStatus(); // Reactive update
                updateFollowersCount(-1);
                sendUnfollowNotification();
            }).addOnFailureListener(e -> btnFollow.setEnabled(true));
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("timestamp", FieldValue.serverTimestamp());

            batch.set(followingRef, data);
            batch.set(followersRef, data);
            batch.update(currentUserRef, "followingCount", FieldValue.increment(1));
            batch.update(targetAuthorRef, "followersCount", FieldValue.increment(1));

            batch.commit().addOnSuccessListener(aVoid -> {
                btnFollow.setEnabled(true);
                btnFollow.setText("Unfollow");
                btnFollow.setBackgroundResource(R.drawable.bg_btn_unfollow);
                checkFollowStatus(); // Reactive update
                updateFollowersCount(1);
                sendFollowNotification();
            }).addOnFailureListener(e -> btnFollow.setEnabled(true));
        }
    }

    private void sendFollowNotification() {
        if (currentUserId == null || authorUid == null) return;

        fStore.collection("Users").document(currentUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String senderName = doc.getString("full_name");
                String senderImage = doc.getString("profile_pic");

                Map<String, Object> notification = new HashMap<>();
                notification.put("senderId", currentUserId);
                notification.put("senderName", senderName != null ? senderName : "Someone");
                notification.put("senderImage", senderImage != null ? senderImage : "");
                notification.put("title", "New Follower");
                notification.put("message", (senderName != null ? senderName : "Someone") + " started following you");
                notification.put("type", "follow");
                notification.put("timestamp", FieldValue.serverTimestamp());
                notification.put("read", false);

                fStore.collection("Notifications").document(authorUid)
                        .collection("UserNotifications").add(notification);
            }
        });
    }

    private void sendUnfollowNotification() {
        if (currentUserId == null || authorUid == null) return;

        fStore.collection("Users").document(currentUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String senderName = doc.getString("full_name");
                String senderImage = doc.getString("profile_pic");

                Map<String, Object> notification = new HashMap<>();
                notification.put("senderId", currentUserId);
                notification.put("senderName", senderName != null ? senderName : "Someone");
                notification.put("senderImage", senderImage != null ? senderImage : "");
                notification.put("title", "Unfollowed");
                notification.put("message", (senderName != null ? senderName : "Someone") + " stopped following you");
                notification.put("type", "unfollow");
                notification.put("timestamp", FieldValue.serverTimestamp());
                notification.put("read", false);

                fStore.collection("Notifications").document(authorUid)
                        .collection("UserNotifications").add(notification);
            }
        });
    }

    private void updateFollowersCount(int change) {
        try {
            long current = Long.parseLong(tvFollowers.getText().toString());
            tvFollowers.setText(String.valueOf(Math.max(0, current + change)));
        } catch (Exception ignored) {}
    }
}
