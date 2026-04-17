package com.example.onlineexamapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.facebook.shimmer.ShimmerFrameLayout;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment {

    private RecyclerView rvDiscoverAll;
    private DiscoveryAdapter adapter;
    private List<DiscoveryActivityModel> allDiscoveries = new ArrayList<>();
    private List<DiscoveryActivityModel> filteredList = new ArrayList<>();
    private FirebaseFirestore fStore;
    private ShimmerFrameLayout shimmerDiscover;
    
    private CardView cvSearch;
    private EditText etSearch;
    private ChipGroup chipGroup;
    private String currentCategory = "All";
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        fStore = FirebaseFirestore.getInstance();
        shimmerDiscover = view.findViewById(R.id.shimmer_discover);
        // llDiscoverContent removed, using rvDiscoverAll for visibility toggle

        // Start Shimmer
        if (shimmerDiscover != null) {
            shimmerDiscover.startShimmer();
        }

        cvSearch = view.findViewById(R.id.cvSearchContainer);
        etSearch = view.findViewById(R.id.etSearchDiscover);
        chipGroup = view.findViewById(R.id.chipGroupCategories);
        ImageView ivCloseSearch = view.findViewById(R.id.ivCloseSearch);
        ImageView ivSearchToggle = view.findViewById(R.id.ivSearch);
        ImageView ivBack = view.findViewById(R.id.ivBack);

        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    // Navigate to Home tab in Bottom Navigation
                    View navHome = getActivity().findViewById(R.id.navHome);
                    if (navHome != null) {
                        navHome.performClick();
                    } else {
                        // Fallback if not in MainHomeActivity
                        getActivity().onBackPressed();
                    }
                }
            });
        }

        ivSearchToggle.setOnClickListener(v -> {
            cvSearch.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        });

        ivCloseSearch.setOnClickListener(v -> {
            cvSearch.setVisibility(View.GONE);
            etSearch.setText("");
            currentQuery = "";
            applyFilter();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().toLowerCase().trim();
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = view.findViewById(checkedId);
            if (chip != null) {
                currentCategory = chip.getText().toString();
                applyFilter();
            }
        });

        rvDiscoverAll = view.findViewById(R.id.recyclerView);
        adapter = new DiscoveryAdapter(getContext(), filteredList);
        rvDiscoverAll.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDiscoverAll.setAdapter(adapter);

        fetchDiscoveries();

        return view;
    }

    private void fetchDiscoveries() {
        fStore.collection("DiscoveryActivities")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allDiscoveries.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        DiscoveryActivityModel model = doc.toObject(DiscoveryActivityModel.class);
                        model.setId(doc.getId());
                        allDiscoveries.add(model);
                    }
                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            boolean matchesQuery = query.isEmpty() || mTitle.contains(query) || mCategory.contains(query);

            if (matchesCategory && matchesQuery) {
                filteredList.add(model);
            }
        }
        
        if (adapter != null) {
            adapter.updateList(new ArrayList<>(filteredList));
        }

        // Hide shimmer and show content once data is loaded
        if (isAdded() && shimmerDiscover != null && allDiscoveries.size() > 0) {
            shimmerDiscover.stopShimmer();
            shimmerDiscover.setVisibility(View.GONE);
            rvDiscoverAll.setVisibility(View.VISIBLE);
        }
    }
}
