package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.facebook.shimmer.ShimmerFrameLayout;
import android.widget.ScrollView;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

public class HomeFragment extends Fragment {

    private RecyclerView rvHomeDiscover, rvHomeAuthors;
    private DashboardAdapter dashboardAdapter;
    private AuthorHomeAdapter authorAdapter;
    private List<DiscoveryActivityModel> discoveryList;
    private List<UserModel> authorList;
    private FirebaseFirestore fStore;
    private TextView tvUserGreeting;
    private ImageView ivHeaderProfile;
    private ShimmerFrameLayout shimmerHome;
    private ScrollView svHomeContent;
    private View llHomeDynamicContent;
    private boolean isDiscoveriesFetched = false;
    private boolean isAuthorsFetched = false;
    private TextView tvChatBadge;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isRefreshing = false;
    private com.google.firebase.firestore.ListenerRegistration chatBadgeListener;

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

        shimmerHome = view.findViewById(R.id.shimmer_home);
        svHomeContent = view.findViewById(R.id.svHomeContent);
        llHomeDynamicContent = view.findViewById(R.id.llHomeDynamicContent);
        tvChatBadge = view.findViewById(R.id.tvChatBadge);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        setupSwipeRefresh();

        // Start Shimmer
        shimmerHome.startShimmer();

        setupDiscoverRecycler();
        setupAuthorsRecycler();
        setupClickListeners(view);

        refreshHome();
        setupChatBadge();

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
        View ivChat = view.findViewById(R.id.ivChat);
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
                if (getContext() != null) {
                    Intent intent = new Intent(getContext(), FindFriendsActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (ivChat != null) {
            ivChat.setOnClickListener(v -> {
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), MessagesListActivity.class));
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

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(R.color.purple_500);
            swipeRefreshLayout.setOnRefreshListener(this::refreshHome);
        }
    }

    private void refreshHome() {
        if (isRefreshing) return;
        isRefreshing = true;

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.post(() -> {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
            });
        }

        // Parallel execution for optimal performance
        List<Task<?>> tasks = new ArrayList<>();
        tasks.add(fetchUserDataTask());
        tasks.add(fetchDiscoveriesTask());
        tasks.add(fetchTopAuthorsTask());

        Tasks.whenAllComplete(tasks).addOnCompleteListener(t -> {
            isRefreshing = false;
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.postDelayed(() -> {
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                }, 500);
            }
            checkLoadingComplete();
        });
    }

    private Task<com.google.firebase.firestore.DocumentSnapshot> fetchUserDataTask() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || fStore == null) return Tasks.forResult(null);

        return fStore.collection("Users")
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
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_user_placeholder)
                                    .error(R.drawable.ic_user_placeholder)
                                    .into(ivHeaderProfile);
                        } catch (Exception ignored) {
                        }
                    }
                });
    }

    private Task<com.google.firebase.firestore.QuerySnapshot> fetchDiscoveriesTask() {
        if (fStore == null || dashboardAdapter == null || discoveryList == null) return Tasks.forResult(null);

        return fStore.collection("DiscoveryActivities")
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
                    isDiscoveriesFetched = true;
                    checkLoadingComplete();
                })
                .addOnFailureListener(e -> {
                    isDiscoveriesFetched = true;
                    checkLoadingComplete();
                });
    }

    private void checkLoadingComplete() {
        if (isDiscoveriesFetched && isAuthorsFetched) {
            if (shimmerHome != null) {
                shimmerHome.stopShimmer();
                shimmerHome.setVisibility(View.GONE);
            }
            if (llHomeDynamicContent != null) {
                llHomeDynamicContent.setVisibility(View.VISIBLE);
            }
        }
    }

    private Task<com.google.firebase.firestore.QuerySnapshot> fetchTopAuthorsTask() {
        if (fStore == null || authorAdapter == null || authorList == null) return Tasks.forResult(null);

        return fStore.collection("Users")
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
                    isAuthorsFetched = true;
                    checkLoadingComplete();
                })
                .addOnFailureListener(e -> {
                    isAuthorsFetched = true;
                    checkLoadingComplete();
                });
    }

    private void setupChatBadge() {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            android.util.Log.w("ChatBadge", "No current user for badge setup");
            return;
        }

        android.util.Log.d("ChatBadge", "Setting up badge for UID: " + uid);

        // Listen to all conversations where the user is a participant
        chatBadgeListener = fStore.collection("Conversations")
                .whereArrayContains("participants", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e("ChatBadge", "Error listening to chats: " + error.getMessage());
                        return;
                    }

                    int totalUnread = 0;
                    if (value != null) {
                        android.util.Log.d("ChatBadge", "Found " + value.size() + " conversations");
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                            Object unreadObj = doc.get("unreadCount");
                            if (unreadObj instanceof java.util.Map) {
                                java.util.Map<?, ?> unreadMap = (java.util.Map<?, ?>) unreadObj;
                                Object countObj = unreadMap.get(uid);
                                if (countObj instanceof Number) {
                                    int count = ((Number) countObj).intValue();
                                    android.util.Log.d("ChatBadge", "Chat " + doc.getId() + " has " + count + " unread for me");
                                    totalUnread += count;
                                }
                            } else {
                                android.util.Log.w("ChatBadge", "unreadCount is not a map for doc: " + doc.getId());
                            }
                        }
                        
                        android.util.Log.d("ChatBadge", "Total unread calculated: " + totalUnread);
                        
                        // Final consistency check before UI update
                        if (!isAdded()) return;
                        
                        if (tvChatBadge != null) {
                            if (totalUnread > 0) {
                                String display = totalUnread > 99 ? "99+" : String.valueOf(totalUnread);
                                tvChatBadge.setText(display);
                                tvChatBadge.setVisibility(View.VISIBLE);
                                android.util.Log.d("ChatBadge", "Badge set to visible with text: " + display);
                            } else {
                                tvChatBadge.setVisibility(View.GONE);
                                android.util.Log.d("ChatBadge", "Badge hidden");
                            }
                        } else {
                            android.util.Log.e("ChatBadge", "tvChatBadge is null in setupChatBadge callback");
                        }
                    } else {
                        android.util.Log.w("ChatBadge", "SnapShot value is null");
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatBadgeListener != null) {
            chatBadgeListener.remove();
        }
    }
}