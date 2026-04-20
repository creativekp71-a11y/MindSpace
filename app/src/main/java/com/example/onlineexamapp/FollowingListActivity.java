package com.example.onlineexamapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FollowingListActivity extends BaseActivity {

    private RecyclerView rvFollowing;
    private AuthorAdapter adapter;
    private List<Author> followedAuthors;
    private FirebaseFirestore fStore;
    private String currentUserId;
    private TextView tvNoFollowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following_list);

        fStore = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        rvFollowing = findViewById(R.id.rvFollowingList);
        tvNoFollowing = findViewById(R.id.tvNoFollowing);
        findViewById(R.id.ivBackFollowing).setOnClickListener(v -> finish());

        followedAuthors = new ArrayList<>();
        adapter = new AuthorAdapter(this, followedAuthors);
        rvFollowing.setLayoutManager(new LinearLayoutManager(this));
        rvFollowing.setAdapter(adapter);

        if (currentUserId != null) {
            loadFollowingList();
        } else {
            finish();
        }
    }

    private void loadFollowingList() {
        fStore.collection("Following").document(currentUserId)
                .collection("UserFollowing")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvNoFollowing.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<String> followedUids = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        followedUids.add(doc.getId());
                    }

                    fetchAuthorDetails(followedUids);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load following list", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchAuthorDetails(List<String> uids) {
        // Since whereIn has a limit, and lists might be long in a real app,
        // but for this MVP we'll fetch them individually or in batches if needed.
        // For simplicity and small lists, we fetch documents for each UID.
        
        followedAuthors.clear();
        for (String uid : uids) {
            fStore.collection("Users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Author author = new Author();
                    author.setUid(doc.getId());
                    author.setFullName(doc.getString("full_name"));
                    author.setUsername(doc.getString("username"));
                    author.setProfilePic(doc.getString("profile_pic"));
                    author.setCoverPic(doc.getString("cover_pic"));
                    author.setBio(doc.getString("bio"));
                    
                    Boolean isAuthor = doc.getBoolean("isAuthor");
                    author.setAuthor(isAuthor != null ? isAuthor : false);
                    
                    Long fCount = doc.getLong("followersCount");
                    author.setFollowersCount(fCount != null ? fCount : 0);
                    
                    Long flCount = doc.getLong("followingCount");
                    author.setFollowingCount(flCount != null ? flCount : 0);

                    followedAuthors.add(author);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }
}
