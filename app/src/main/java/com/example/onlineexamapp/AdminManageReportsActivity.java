package com.example.onlineexamapp;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminManageReportsActivity extends AppCompatActivity {

    private RecyclerView rvReports;
    private AdminReportAdapter adapter;
    private List<ReportModel> reportList;
    private FirebaseFirestore fStore;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout llEmptyState;
    
    private TextView tvFilterAll, tvFilterContent, tvFilterUsers;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_reports);
        // Immersive Status Bar Fix
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        fStore = FirebaseFirestore.getInstance();
        
        rvReports = findViewById(R.id.rvReports);
        swipeRefresh = findViewById(R.id.swipeRefreshReports);
        llEmptyState = findViewById(R.id.llEmptyState);
        View btnBack = findViewById(R.id.btnBack);
        
        tvFilterAll = findViewById(R.id.tvFilterAll);
        tvFilterContent = findViewById(R.id.tvFilterContent);
        tvFilterUsers = findViewById(R.id.tvFilterUsers);

        rvReports.setLayoutManager(new LinearLayoutManager(this));
        reportList = new ArrayList<>();
        adapter = new AdminReportAdapter(this, reportList);
        rvReports.setAdapter(adapter);

        loadReports();

        swipeRefresh.setOnRefreshListener(this::loadReports);
        btnBack.setOnClickListener(v -> finish());
        
        tvFilterAll.setOnClickListener(v -> applyFilter("all"));
        tvFilterContent.setOnClickListener(v -> applyFilter("content"));
        tvFilterUsers.setOnClickListener(v -> applyFilter("user"));
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        
        // Update UI Tabs
        tvFilterAll.setTextColor(getResources().getColor(filter.equals("all") ? android.R.color.holo_blue_dark : android.R.color.darker_gray));
        tvFilterContent.setTextColor(getResources().getColor(filter.equals("content") ? android.R.color.holo_blue_dark : android.R.color.darker_gray));
        tvFilterUsers.setTextColor(getResources().getColor(filter.equals("user") ? android.R.color.holo_blue_dark : android.R.color.darker_gray));
        
        loadReports();
    }

    private void loadReports() {
        swipeRefresh.setRefreshing(true);
        
        Query query = fStore.collection("Reports")
                .whereEqualTo("status", "pending")
                .orderBy("timestamp", Query.Direction.DESCENDING);
        
        if (!currentFilter.equals("all")) {
            query = fStore.collection("Reports")
                    .whereEqualTo("status", "pending")
                    .whereEqualTo("targetType", currentFilter)
                    .orderBy("timestamp", Query.Direction.DESCENDING);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            reportList.clear();
            for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                ReportModel model = doc.toObject(ReportModel.class);
                if (model != null) {
                    model.setId(doc.getId());
                    reportList.add(model);
                }
            }
            adapter.notifyDataSetChanged();
            swipeRefresh.setRefreshing(false);
            
            if (reportList.isEmpty()) {
                llEmptyState.setVisibility(View.VISIBLE);
                rvReports.setVisibility(View.GONE);
            } else {
                llEmptyState.setVisibility(View.GONE);
                rvReports.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            swipeRefresh.setRefreshing(false);
            
            // Handle index requirement for first run
            if (e.getMessage() != null && e.getMessage().contains("The query requires an index")) {
                loadSimpleFallback();
            } else {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSimpleFallback() {
        // Fallback without server-side ordering/filtering if index is missing
        fStore.collection("Reports")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reportList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        ReportModel model = doc.toObject(ReportModel.class);
                        if (model != null) {
                            model.setId(doc.getId());
                            
                            // Apply filter in memory for fallback
                            if (currentFilter.equals("all") || model.getTargetType().equalsIgnoreCase(currentFilter)) {
                                reportList.add(model);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                    
                    if (reportList.isEmpty()) {
                        llEmptyState.setVisibility(View.VISIBLE);
                        rvReports.setVisibility(View.GONE);
                    } else {
                        llEmptyState.setVisibility(View.GONE);
                        rvReports.setVisibility(View.VISIBLE);
                    }
                });
    }
}
