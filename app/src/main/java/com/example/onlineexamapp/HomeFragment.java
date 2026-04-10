package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        fStore = FirebaseFirestore.getInstance();
        tvUserGreeting = view.findViewById(R.id.tvUserGreeting);
        ivHeaderProfile = view.findViewById(R.id.ivHeaderProfilePic);

        // Discovery RecyclerView
        rvHomeDiscover = view.findViewById(R.id.rvHomeDiscover);
        rvHomeDiscover.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        discoveryList = new ArrayList<>();
        dashboardAdapter = new DashboardAdapter(getContext(), discoveryList);
        rvHomeDiscover.setAdapter(dashboardAdapter);

        // Authors RecyclerView
        rvHomeAuthors = view.findViewById(R.id.rvHomeAuthors);
        rvHomeAuthors.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        authorList = new ArrayList<>();
        authorAdapter = new AuthorHomeAdapter(getContext(), authorList);
        rvHomeAuthors.setAdapter(authorAdapter);

        // --- View All Connections ---
        view.findViewById(R.id.tvViewAllDiscover).setOnClickListener(v -> ((MainHomeActivity)requireActivity()).findViewById(R.id.navDiscover).performClick());
        view.findViewById(R.id.tvViewAllAuthors).setOnClickListener(v -> startActivity(new Intent(getActivity(), TopAuthorsActivity.class)));
        view.findViewById(R.id.btnFindFriendsBanner).setOnClickListener(v -> startActivity(new Intent(getActivity(), FindFriendsActivity.class)));
        view.findViewById(R.id.ivSearch).setOnClickListener(v -> startActivity(new Intent(getActivity(), SearchActivity.class)));
        view.findViewById(R.id.ivBell).setOnClickListener(v -> Toast.makeText(getContext(), "You have 0 new notifications 🔔", Toast.LENGTH_SHORT).show());
        ivHeaderProfile.setOnClickListener(v -> ((MainHomeActivity)requireActivity()).findViewById(R.id.navProfile).performClick());

        loadUserData();
        fetchDiscoveries();
        fetchTopAuthors();

        return view;
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            fStore.collection("Users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("full_name");
                    String pic = doc.getString("profile_pic");
                    if (name != null) tvUserGreeting.setText("Hi, " + name + " 👋");
                    if (pic != null && !pic.isEmpty()) {
                        try {
                            byte[] bytes = Base64.decode(pic, Base64.DEFAULT);
                            Glide.with(this).load(bytes).placeholder(R.drawable.ic_user_placeholder).into(ivHeaderProfile);
                        } catch (Exception ignored) {}
                    }
                }
            });
        }
    }

    private void fetchDiscoveries() {
        fStore.collection("DiscoveryActivities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
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
        fStore.collection("Users")
                .whereEqualTo("isAuthor", true)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
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
