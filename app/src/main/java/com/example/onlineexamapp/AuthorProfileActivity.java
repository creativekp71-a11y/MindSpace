package com.example.onlineexamapp;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
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
    private TextView tvFollowers, tvFollowing, tvDiscoveries;
    private AppCompatButton btnFollow;
    private RecyclerView rvDiscoveries;
    private DiscoveryAdapter adapter;
    private List<DiscoveryActivityModel> discoveryList;

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
    }

    private void initUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
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
        btnFollow = findViewById(R.id.btnAuthorFollow);

        rvDiscoveries = findViewById(R.id.rvAuthorDiscoveries);
        rvDiscoveries.setLayoutManager(new LinearLayoutManager(this));
        discoveryList = new ArrayList<>();
        adapter = new DiscoveryAdapter(this, discoveryList);
        rvDiscoveries.setAdapter(adapter);

        // Hide follow button if viewing own profile
        if (authorUid.equals(currentUserId)) {
            btnFollow.setVisibility(View.GONE);
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

                tvFullName.setText(fullName != null ? fullName : "Unknown Author");
                tvUsername.setText(username != null ? "@" + username : "");
                tvBio.setText(bio != null && !bio.isEmpty() ? bio : "No bio available.");
                tvFollowers.setText(String.valueOf(followers != null ? followers : 0));
                tvFollowing.setText(String.valueOf(following != null ? following : 0));

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
                    } else {
                        btnFollow.setText("Follow");
                        btnFollow.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                });
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
                updateFollowersCount(-1);
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
                updateFollowersCount(1);
            }).addOnFailureListener(e -> btnFollow.setEnabled(true));
        }
    }

    private void updateFollowersCount(int change) {
        try {
            long current = Long.parseLong(tvFollowers.getText().toString());
            tvFollowers.setText(String.valueOf(Math.max(0, current + change)));
        } catch (Exception ignored) {}
    }
}
