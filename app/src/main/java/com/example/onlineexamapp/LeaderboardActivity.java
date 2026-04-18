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
import com.facebook.shimmer.ShimmerFrameLayout;
import android.widget.ScrollView;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter adapter;
    private List<UserModel> userList;
    private FirebaseFirestore fStore;
    private ShimmerFrameLayout shimmerLeaderboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        fStore = FirebaseFirestore.getInstance();
        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        shimmerLeaderboard = findViewById(R.id.shimmer_leaderboard);
        // svLeaderboardContent removed, using rvLeaderboard for visibility toggle
        
        // Start Shimmer
        shimmerLeaderboard.startShimmer();
        
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
        BottomNavigationHelper.setupBottomNavigation(this, R.id.navLeaderboard);
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
                    
                    // Stop Shimmer and show content
                    shimmerLeaderboard.stopShimmer();
                    shimmerLeaderboard.setVisibility(View.GONE);
                    rvLeaderboard.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Log.e("LeaderboardError", "Error fetching rankings: " + e.getMessage());
                    Toast.makeText(this, "Failed to load rankings!", Toast.LENGTH_SHORT).show();
                });
    }
}