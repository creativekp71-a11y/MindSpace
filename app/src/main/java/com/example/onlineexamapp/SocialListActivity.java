package com.example.onlineexamapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class SocialListActivity extends AppCompatActivity {

    private RecyclerView rvSocial;
    private AuthorAdapter adapter;
    private List<Author> userList;
    private FirebaseFirestore fStore;
    private String targetUserId, listType;
    private TextView tvTitle, tvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_list);

        fStore = FirebaseFirestore.getInstance();
        targetUserId = getIntent().getStringExtra("userId");
        listType = getIntent().getStringExtra("type"); // "followers" or "following"

        if (targetUserId == null || listType == null) {
            finish();
            return;
        }

        rvSocial = findViewById(R.id.rvSocialList);
        tvTitle = findViewById(R.id.tvSocialTitle);
        tvNoData = findViewById(R.id.tvNoData);
        findViewById(R.id.ivBackSocial).setOnClickListener(v -> finish());

        tvTitle.setText(listType.equalsIgnoreCase("followers") ? "Followers" : "Following");
        tvNoData.setText(listType.equalsIgnoreCase("followers") ? "No Followers" : "Not Following");

        userList = new ArrayList<>();
        adapter = new AuthorAdapter(this, userList);
        rvSocial.setLayoutManager(new LinearLayoutManager(this));
        rvSocial.setAdapter(adapter);

        loadSocialList();
    }

    private void loadSocialList() {
        String collectionPath = listType.equalsIgnoreCase("followers") ? "Followers" : "Following";
        String subCollectionPath = listType.equalsIgnoreCase("followers") ? "UserFollowers" : "UserFollowing";

        fStore.collection(collectionPath).document(targetUserId)
                .collection(subCollectionPath)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvNoData.setVisibility(View.VISIBLE);
                        rvSocial.setVisibility(View.GONE);
                        return;
                    }

                    tvNoData.setVisibility(View.GONE);
                    rvSocial.setVisibility(View.VISIBLE);
                    List<String> uids = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        uids.add(doc.getId());
                    }

                    fetchUserDetails(uids);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load list", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUserDetails(List<String> uids) {
        userList.clear();
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

                    userList.add(author);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }
}
