package com.example.onlineexamapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.cardview.widget.CardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

public class DiscoverActivity extends AppCompatActivity {

    private RecyclerView rvDiscoverAll;
    private DiscoveryAdapter adapter;
    private List<DiscoveryActivityModel> allDiscoveries = new ArrayList<>();
    private List<DiscoveryActivityModel> filteredList = new ArrayList<>();
    private FirebaseFirestore fStore;
    
    // UI for Search/Filter
    private CardView cvSearch;
    private EditText etSearch;
    private ChipGroup chipGroup;
    private String currentCategory = "All";
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        // 🎨 Explicit Purple Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#6C5CE7"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        // 🔥 Firebase init
        fStore = FirebaseFirestore.getInstance();

        // 🔙 Back Button
        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        // 🔥 UI Setup
        cvSearch = findViewById(R.id.cvSearchContainer);
        etSearch = findViewById(R.id.etSearchDiscover);
        chipGroup = findViewById(R.id.chipGroupCategories);
        ImageView ivCloseSearch = findViewById(R.id.ivCloseSearch);

        // 🔍 Search Toggle
        ImageView ivSearch = findViewById(R.id.ivSearch);
        ivSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Search Mode On", Toast.LENGTH_SHORT).show();
            cvSearch.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        });

        ivCloseSearch.setOnClickListener(v -> {
            cvSearch.setVisibility(View.GONE);
            etSearch.setText("");
            currentQuery = "";
            applyFilter();
            // Hide keyboard on close
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        });

        // ⌨️ Search Typing Listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().toLowerCase().trim();
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // ⌨️ Search Keyboard Action
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            applyFilter();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            return true;
        });

        // 🎫 Chip Filter Listener
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = findViewById(checkedId);
            if (chip != null) {
                currentCategory = chip.getText().toString();
                applyFilter();
            }
        });

        // 🔥 RecyclerView Setup
        rvDiscoverAll = findViewById(R.id.recyclerView);
        adapter = new DiscoveryAdapter(this, filteredList);
        rvDiscoverAll.setLayoutManager(new LinearLayoutManager(this));
        rvDiscoverAll.setAdapter(adapter);

        // 🔥 Data Fetch
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDiscoveries();
    }

    private void fetchDiscoveries() {

        fStore.collection("DiscoveryActivities")
                .get() // Temporarily removed orderBy to check if index is the issue
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    
                    int count = queryDocumentSnapshots.size();
                    Toast.makeText(this, "Fetched " + count + " discoveries", Toast.LENGTH_SHORT).show();

                    allDiscoveries.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        DiscoveryActivityModel model = doc.toObject(DiscoveryActivityModel.class);
                        model.setId(doc.getId());
                        allDiscoveries.add(model);
                    }
                    applyFilter();
                })
                .addOnFailureListener(e -> {
                        Toast.makeText(this, "Fetch Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        android.util.Log.e("DiscoverDebug", "Fetch failed", e);
                });
    }

    private void applyFilter() {
        filteredList.clear();
        String query = currentQuery.toLowerCase().trim();
        
        for (DiscoveryActivityModel model : allDiscoveries) {
            String mTitle = model.getTitle() != null ? model.getTitle().toLowerCase().trim() : "";
            String mCategory = model.getCategory() != null ? model.getCategory().toLowerCase().trim() : "";
            String filterCat = currentCategory.toLowerCase().trim();
            
            boolean matchesCategory = filterCat.equals("all") || 
                    mCategory.equals(filterCat);
            
            boolean matchesQuery = query.isEmpty() || 
                    mTitle.contains(query) ||
                    mCategory.contains(query);

            if (matchesCategory && matchesQuery) {
                filteredList.add(model);
            }
        }
        
        if (adapter != null) {
            adapter.updateList(new ArrayList<>(filteredList)); // Pass a fresh copy to be safe
        }
    }
}