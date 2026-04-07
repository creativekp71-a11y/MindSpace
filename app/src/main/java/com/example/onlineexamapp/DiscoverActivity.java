package com.example.onlineexamapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class DiscoverActivity extends AppCompatActivity {

    private RecyclerView rvDiscoverAll;
    private DiscoveryAdapter discoveryAdapter;
    private List<DiscoveryActivityModel> discoveryList;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        fStore = FirebaseFirestore.getInstance();

        // 1. Back button logic
        ImageView ivBack = findViewById(R.id.ivBackDiscover);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        // 2. Search icon logic
        ImageView ivSearch = findViewById(R.id.ivSearchDiscover);
        if (ivSearch != null) {
            ivSearch.setOnClickListener(v -> Toast.makeText(DiscoverActivity.this, "Opening Search...", Toast.LENGTH_SHORT).show());
        }

        // 3. RecyclerView Setup
        rvDiscoverAll = findViewById(R.id.rvDiscoverAll);
        discoveryList = new ArrayList<>();
        discoveryAdapter = new DiscoveryAdapter(this, discoveryList, false); // Vertical layout
        rvDiscoverAll.setAdapter(discoveryAdapter);

        fetchDiscoveries();
    }

    private void fetchDiscoveries() {
        fStore.collection("DiscoveryActivities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    discoveryList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DiscoveryActivityModel model = document.toObject(DiscoveryActivityModel.class);
                        model.setId(document.getId());
                        discoveryList.add(model);
                    }
                    discoveryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching discoveries: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}