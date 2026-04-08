package com.example.onlineexamapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class DiscoverActivity extends AppCompatActivity {

    private RecyclerView rvDiscoverAll;
    private DiscoveryAdapter adapter;
    private List<DiscoveryActivityModel> list;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        // 🔥 Firebase init
        fStore = FirebaseFirestore.getInstance();

        // 🔙 Back Button
        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        // 🔍 Search Button
        ImageView ivSearch = findViewById(R.id.ivSearch);
        if (ivSearch != null) {
            ivSearch.setOnClickListener(v ->
                    Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show());
        }

        // 🔥 RecyclerView Setup
        rvDiscoverAll = findViewById(R.id.recyclerView);

        list = new ArrayList<>();
        adapter = new DiscoveryAdapter(this, list);

        rvDiscoverAll.setLayoutManager(new LinearLayoutManager(this));
        rvDiscoverAll.setAdapter(adapter);

        // 🔥 Data Fetch
        fetchDiscoveries();
    }

    private void fetchDiscoveries() {

        fStore.collection("DiscoveryActivities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    list.clear();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        DiscoveryActivityModel model = doc.toObject(DiscoveryActivityModel.class);
                        model.setId(doc.getId());

                        list.add(model);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}