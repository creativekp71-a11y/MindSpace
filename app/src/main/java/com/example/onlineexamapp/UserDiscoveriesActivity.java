package com.example.onlineexamapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class UserDiscoveriesActivity extends AppCompatActivity {

    private RecyclerView rvDiscoveries;
    private DiscoveryAdapter adapter;
    private List<DiscoveryActivityModel> discoveryList;
    private FirebaseFirestore fStore;
    private String targetUserId;
    private TextView tvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_discoveries);

        fStore = FirebaseFirestore.getInstance();
        targetUserId = getIntent().getStringExtra("userId");

        if (targetUserId == null) {
            finish();
            return;
        }

        rvDiscoveries = findViewById(R.id.rvUserDiscoveries);
        tvNoData = findViewById(R.id.tvNoDiscoveries);
        findViewById(R.id.ivBackDiscoveries).setOnClickListener(v -> finish());

        discoveryList = new ArrayList<>();
        adapter = new DiscoveryAdapter(this, discoveryList);
        rvDiscoveries.setLayoutManager(new LinearLayoutManager(this));
        rvSocialPadding(); // Custom padding for list
        rvDiscoveries.setAdapter(adapter);

        loadDiscoveries();
    }

    private void rvSocialPadding() {
        // Simple helper or leave default
    }

    private void loadDiscoveries() {
        fStore.collection("DiscoveryActivities")
                .whereEqualTo("authorId", targetUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    discoveryList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvNoData.setVisibility(View.VISIBLE);
                        rvDiscoveries.setVisibility(View.GONE);
                    } else {
                        tvNoData.setVisibility(View.GONE);
                        rvDiscoveries.setVisibility(View.VISIBLE);
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            DiscoveryActivityModel model = doc.toObject(DiscoveryActivityModel.class);
                            if (model != null) {
                                model.setId(doc.getId());
                                discoveryList.add(model);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Fallback in case of index issues
                    fStore.collection("DiscoveryActivities")
                            .whereEqualTo("authorId", targetUserId)
                            .get()
                            .addOnSuccessListener(snap -> {
                            discoveryList.clear();
                            if (snap.isEmpty()) {
                                tvNoData.setVisibility(View.VISIBLE);
                                rvDiscoveries.setVisibility(View.GONE);
                            } else {
                                tvNoData.setVisibility(View.GONE);
                                rvDiscoveries.setVisibility(View.VISIBLE);
                                for (com.google.firebase.firestore.DocumentSnapshot doc : snap) {
                                        DiscoveryActivityModel model = doc.toObject(DiscoveryActivityModel.class);
                                        if (model != null) {
                                            model.setId(doc.getId());
                                            discoveryList.add(model);
                                        }
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            });
                });
    }
}
