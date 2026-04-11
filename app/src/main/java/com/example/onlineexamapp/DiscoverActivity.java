package com.example.onlineexamapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DiscoverActivity extends AppCompatActivity {

    private RecyclerView rvDiscoverAll;
    private DiscoveryAdapter adapter;
    private List<DiscoveryActivityModel> allDiscoveries = new ArrayList<>();
    private List<DiscoveryActivityModel> filteredList = new ArrayList<>();
    private FirebaseFirestore fStore;

    private CardView cvSearch;
    private EditText etSearch;
    private ChipGroup chipGroup;
    private String currentCategory = "All";
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#6C5CE7"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(
                        window.getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            }
        }

        fStore = FirebaseFirestore.getInstance();

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                Intent intent = new Intent(DiscoverActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            });
        }

        cvSearch = findViewById(R.id.cvSearchContainer);
        etSearch = findViewById(R.id.etSearchDiscover);
        chipGroup = findViewById(R.id.chipGroupCategories);
        ImageView ivCloseSearch = findViewById(R.id.ivCloseSearch);

        ImageView ivSearch = findViewById(R.id.ivSearch);
        if (ivSearch != null) {
            ivSearch.setOnClickListener(v -> {
                Toast.makeText(this, "Search Mode On", Toast.LENGTH_SHORT).show();
                cvSearch.setVisibility(View.VISIBLE);
                etSearch.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }

        if (ivCloseSearch != null) {
            ivCloseSearch.setOnClickListener(v -> {
                cvSearch.setVisibility(View.GONE);
                etSearch.setText("");
                currentQuery = "";
                applyFilter();

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                }
            });
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().toLowerCase().trim();
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            applyFilter();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
            return true;
        });

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = findViewById(checkedId);
            if (chip != null) {
                currentCategory = chip.getText().toString();
                applyFilter();
            }
        });

        rvDiscoverAll = findViewById(R.id.recyclerView);
        adapter = new DiscoveryAdapter(this, filteredList);
        rvDiscoverAll.setLayoutManager(new LinearLayoutManager(this));
        rvDiscoverAll.setAdapter(adapter);

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(DiscoverActivity.this, DashboardActivity.class));
            finish();
        });

        findViewById(R.id.navDiscover).setOnClickListener(v -> {
            // Already here
        });

        findViewById(R.id.navLeaderboard).setOnClickListener(v -> {
            startActivity(new Intent(DiscoverActivity.this, LeaderboardActivity.class));
            finish();
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(DiscoverActivity.this, ProfileActivity.class));
            finish();
        });

        findViewById(R.id.ivCenterLogo).setOnClickListener(v -> {
            Intent intent = new Intent(DiscoverActivity.this, QuizActivity.class);
            intent.putExtra("QUIZ_CATEGORY", "Quick Play");
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDiscoveries();
    }

    private void fetchDiscoveries() {
        fStore.collection("DiscoveryActivities")
                .get()
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

            boolean matchesCategory = filterCat.equals("all") || mCategory.equals(filterCat);

            boolean matchesQuery = query.isEmpty()
                    || mTitle.contains(query)
                    || mCategory.contains(query);

            if (matchesCategory && matchesQuery) {
                filteredList.add(model);
            }
        }

        if (adapter != null) {
            adapter.updateList(new ArrayList<>(filteredList));
        }
    }
}