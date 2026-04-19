package com.example.onlineexamapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.facebook.shimmer.ShimmerFrameLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.List;

public class RankFragment extends Fragment {

    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter adapter;
    private List<UserModel> userList;
    private FirebaseFirestore fStore;
    private ShimmerFrameLayout shimmerLeaderboard;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rank, container, false);

        fStore = FirebaseFirestore.getInstance();
        rvLeaderboard = view.findViewById(R.id.rvLeaderboard);
        shimmerLeaderboard = view.findViewById(R.id.shimmer_leaderboard);
        swipeRefresh = view.findViewById(R.id.swipeRefreshLeaderboard);
        
        // Start Shimmer
        if (shimmerLeaderboard != null) {
            shimmerLeaderboard.startShimmer();
        }

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::fetchLeaderboardData);
            swipeRefresh.setColorSchemeResources(R.color.purple_500);
        }

        userList = new ArrayList<>();
        
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaderboardAdapter(getContext(), userList);
        rvLeaderboard.setAdapter(adapter);

        fetchLeaderboardData();

        return view;
    }

    private void fetchLeaderboardData() {
        fStore.collection("Users")
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserModel user = document.toObject(UserModel.class);
                        
                        // Stealth Admin: Hide this specific account from public rankings
                        if (user.getEmail() != null && user.getEmail().equalsIgnoreCase("admin@mindspace.com")) {
                            continue;
                        }
                        
                        user.setId(document.getId());
                        userList.add(user);
                    }
                    adapter.notifyDataSetChanged();

                    // Stop Shimmer and show content
                    if (isAdded() && shimmerLeaderboard != null) {
                        shimmerLeaderboard.stopShimmer();
                        shimmerLeaderboard.setVisibility(View.GONE);
                        rvLeaderboard.setVisibility(View.VISIBLE);
                    }

                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LeaderboardError", "Error fetching rankings: " + e.getMessage());
                    if (isAdded() && shimmerLeaderboard != null) {
                        shimmerLeaderboard.stopShimmer();
                        shimmerLeaderboard.setVisibility(View.GONE);
                    }
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                    if (getContext() != null) Toast.makeText(getContext(), "Failed to load rankings!", Toast.LENGTH_SHORT).show();
                });
    }
}
