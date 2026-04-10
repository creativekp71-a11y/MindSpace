package com.example.onlineexamapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter adapter;
    private List<UserModel> userList;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        fStore = FirebaseFirestore.getInstance();
        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        userList = new ArrayList<>();
        
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(this, userList);
        rvLeaderboard.setAdapter(adapter);

        fetchLeaderboardData();

        findViewById(R.id.ivBackLeaderboard).setOnClickListener(v -> finish());

        // --- Bottom Navigation ---
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new android.content.Intent(LeaderboardActivity.this, DashboardActivity.class));
            finish();
        });

        findViewById(R.id.navDiscover).setOnClickListener(v -> {
            startActivity(new android.content.Intent(LeaderboardActivity.this, DiscoverActivity.class));
            finish();
        });

        findViewById(R.id.navLeaderboard).setOnClickListener(v -> {
            // Already here
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new android.content.Intent(LeaderboardActivity.this, ProfileActivity.class));
            finish();
        });

        findViewById(R.id.ivCenterLogo).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(LeaderboardActivity.this, QuizActivity.class);
            intent.putExtra("QUIZ_CATEGORY", "Quick Play");
            startActivity(intent);
        });
    }

    private void fetchLeaderboardData() {
        // Fetch users ordered by points (Descending)
        fStore.collection("Users")
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(50) // Top 50 users
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserModel user = document.toObject(UserModel.class);
                        user.setId(document.getId());
                        userList.add(user);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("LeaderboardError", "Error fetching rankings: " + e.getMessage());
                    Toast.makeText(this, "Failed to load rankings!", Toast.LENGTH_SHORT).show();
                });
    }
}