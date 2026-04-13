package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvHomeDiscover, rvHomeAuthors;
    private DashboardAdapter dashboardAdapter;
    private AuthorHomeAdapter authorAdapter;
    private List<DiscoveryActivityModel> discoveryList;
    private List<UserModel> authorList;
    private FirebaseFirestore fStore;
    private TextView tvUserGreeting;
    private ImageView ivHeaderProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        fStore = FirebaseFirestore.getInstance();

        tvUserGreeting = view.findViewById(R.id.tvUserGreeting);
        ivHeaderProfile = view.findViewById(R.id.ivHeaderProfilePic);

        rvHomeDiscover = view.findViewById(R.id.rvHomeDiscover);
        rvHomeAuthors = view.findViewById(R.id.rvHomeAuthors);

        setupDiscoverRecycler();
        setupAuthorsRecycler();
        setupClickListeners(view);

        loadUserData();
        fetchDiscoveries();
        fetchTopAuthors();

        return view;
    }

    private void setupDiscoverRecycler() {
        if (getContext() == null || rvHomeDiscover == null) return;

        rvHomeDiscover.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        discoveryList = new ArrayList<>();
        dashboardAdapter = new DashboardAdapter(getContext(), discoveryList);
        rvHomeDiscover.setAdapter(dashboardAdapter);
        rvHomeDiscover.setNestedScrollingEnabled(false);
    }

    private void setupAuthorsRecycler() {
        if (getContext() == null || rvHomeAuthors == null) return;

        rvHomeAuthors.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        authorList = new ArrayList<>();
        authorAdapter = new AuthorHomeAdapter(getContext(), authorList);
        rvHomeAuthors.setAdapter(authorAdapter);
        rvHomeAuthors.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners(View view) {
        View tvViewAllDiscover = view.findViewById(R.id.tvViewAllDiscover);
        View tvViewAllAuthors = view.findViewById(R.id.tvViewAllAuthors);
        View btnFindFriendsBanner = view.findViewById(R.id.btnFindFriendsBanner);
        View ivSearch = view.findViewById(R.id.ivSearch);
        View ivBell = view.findViewById(R.id.ivBell);

        if (tvViewAllDiscover != null) {
            tvViewAllDiscover.setOnClickListener(v -> {
                if (getActivity() instanceof MainHomeActivity) {
                    View navDiscover = getActivity().findViewById(R.id.navDiscover);
                    if (navDiscover != null) {
                        navDiscover.performClick();
                    }
                }
            });
        }

        if (tvViewAllAuthors != null) {
            tvViewAllAuthors.setOnClickListener(v -> {
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), TopAuthorsActivity.class));
                }
            });
        }

        if (btnFindFriendsBanner != null) {
            btnFindFriendsBanner.setOnClickListener(v -> {
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), FindFriendsActivity.class));
                }
            });
        }

        if (ivSearch != null) {
            ivSearch.setOnClickListener(v -> {
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), SearchActivity.class));
                }
            });
        }

        if (ivBell != null) {
            ivBell.setOnClickListener(v -> {
                if (getActivity() instanceof MainHomeActivity) {
                    ((MainHomeActivity) getActivity()).openNotifications();
                }
            });
        }

        if (ivHeaderProfile != null) {
            ivHeaderProfile.setOnClickListener(v -> {
                if (getActivity() instanceof MainHomeActivity) {
                    View navProfile = getActivity().findViewById(R.id.navProfile);
                    if (navProfile != null) {
                        navProfile.performClick();
                    }
                }
            });
        }
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || fStore == null) return;

        fStore.collection("Users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || doc == null || !doc.exists()) return;

                    String name = doc.getString("full_name");
                    String pic = doc.getString("profile_pic");

                    if (tvUserGreeting != null && name != null && !name.trim().isEmpty()) {
                        tvUserGreeting.setText("Hi, " + name + " 👋");
                    }

                    if (ivHeaderProfile != null && pic != null && !pic.isEmpty()) {
                        try {
                            byte[] bytes = Base64.decode(pic, Base64.DEFAULT);
                            Glide.with(this)
                                    .load(bytes)
                                    .placeholder(R.drawable.ic_user_placeholder)
                                    .error(R.drawable.ic_user_placeholder)
                                    .into(ivHeaderProfile);
                        } catch (Exception ignored) {
                        }
                    }
                });
    }

    private void fetchDiscoveries() {
        if (fStore == null || dashboardAdapter == null || discoveryList == null) return;

        fStore.collection("DiscoveryActivities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    discoveryList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DiscoveryActivityModel model = document.toObject(DiscoveryActivityModel.class);
                        model.setId(document.getId());
                        discoveryList.add(model);
                    }
                    dashboardAdapter.notifyDataSetChanged();
                });
    }

    private void fetchTopAuthors() {
        if (fStore == null || authorAdapter == null || authorList == null) return;

        fStore.collection("Users")
                .whereEqualTo("isAuthor", true)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    authorList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserModel author = document.toObject(UserModel.class);
                        author.setId(document.getId());
                        authorList.add(author);
                    }
                    authorAdapter.notifyDataSetChanged();
                });
    }
}