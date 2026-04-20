package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FindFriendsActivity extends BaseActivity {

    private ImageView ivBack;
    private EditText etSearchFriends;
    private RelativeLayout btnInviteMenu;
    private RecyclerView rvFriends;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private FriendAdapter adapter;

    private final List<FriendModel> allFriendsList = new ArrayList<>();
    private final List<FriendModel> filteredFriendsList = new ArrayList<>();
    private final Set<String> followingIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        ivBack = findViewById(R.id.ivBack);
        etSearchFriends = findViewById(R.id.etSearchFriends);
        btnInviteMenu = findViewById(R.id.btnInviteMenu);
        rvFriends = findViewById(R.id.rvFriends);

        if (rvFriends != null) {
            rvFriends.setLayoutManager(new LinearLayoutManager(this));
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (rvFriends != null && currentUser != null) {
            adapter = new FriendAdapter(this, filteredFriendsList, currentUser.getUid());
            rvFriends.setAdapter(adapter);
        }

        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        if (btnInviteMenu != null) {
            btnInviteMenu.setOnClickListener(v -> sendInvite());
        }

        if (etSearchFriends != null) {
            etSearchFriends.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFriends(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        }

        loadFollowingThenUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFollowingThenUsers();
    }

    private void sendInvite() {
        String inviteMessage = "Hey! Try my MindSpace Quiz App 🎯\n\n"
                + "Download APK here:\n"
                +"https://drive.google.com/file/d/1ZdDu-hC4uGPqIPC44IaIgWd-F0HxqnGX/view?usp=sharing";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Invite Friends");
        intent.putExtra(Intent.EXTRA_TEXT, inviteMessage);

        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void loadFollowingThenUsers() {
        if (currentUser == null) return;

        db.collection("Following")
                .document(currentUser.getUid())
                .collection("UserFollowing")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isFinishing() || isDestroyed()) return;
                    followingIds.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        followingIds.add(doc.getId());
                    }

                    loadAllUsers();
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    followingIds.clear();
                    loadAllUsers();
                });
    }

    private void loadAllUsers() {
        if (currentUser == null) return;

        db.collection("Users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isFinishing() || isDestroyed()) return;
                    
                    allFriendsList.clear();
                    filteredFriendsList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String userId = doc.getId();

                        if (userId.equals(currentUser.getUid())) {
                            continue;
                        }

                        String fullName = doc.getString("full_name");
                        String username = doc.getString("username");
                        String email = doc.getString("email");

                        // Stealth Admin: Hide this specific account from friend discovery
                        if (email != null && email.equalsIgnoreCase("admin@mindspace.com")) {
                            continue;
                        }
                        String profilePic = doc.getString("profile_pic");

                        String displayName;
                        if (fullName != null && !fullName.trim().isEmpty()) {
                            displayName = fullName;
                        } else if (username != null && !username.trim().isEmpty()) {
                            displayName = username;
                        } else {
                            displayName = "User";
                        }

                        boolean isFollowed = followingIds.contains(userId);

                        FriendModel model = new FriendModel(
                                userId,
                                displayName,
                                email != null ? email : "",
                                "",
                                profilePic != null ? profilePic : "",
                                isFollowed
                        );

                        allFriendsList.add(model);
                    }

                    filteredFriendsList.addAll(allFriendsList);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    Toast.makeText(FindFriendsActivity.this, "Users found: " + filteredFriendsList.size(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(FindFriendsActivity.this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void filterFriends(@NonNull String query) {
        filteredFriendsList.clear();

        if (query.isEmpty()) {
            filteredFriendsList.addAll(allFriendsList);
        } else {
            String lower = query.toLowerCase();

            for (FriendModel friend : allFriendsList) {
                if (friend.getName().toLowerCase().contains(lower)
                        || friend.getEmail().toLowerCase().contains(lower)
                        || friend.getPhone().toLowerCase().contains(lower)) {
                    filteredFriendsList.add(friend);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
