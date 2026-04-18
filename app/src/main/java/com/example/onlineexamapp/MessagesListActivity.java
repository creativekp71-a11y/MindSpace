package com.example.onlineexamapp;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MessagesListActivity extends AppCompatActivity {

    private RecyclerView rvMessagesList;
    private LinearLayout layoutEmptyChats, layoutNoResults;
    private android.widget.EditText etSearch;
    private MessagesListAdapter adapter;
    private List<ConversationModel> conversationList;
    private List<UserModel> connectedUsers = new ArrayList<>();
    private java.util.Set<String> connectedUserIds = new java.util.HashSet<>();
    private List<Object> displayList = new ArrayList<>();
    
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    private ListenerRegistration conversationsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_list);

        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initUI();
    }

    private void initUI() {
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        rvMessagesList = findViewById(R.id.rvMessagesList);
        layoutEmptyChats = findViewById(R.id.layoutEmptyChats);
        layoutNoResults = findViewById(R.id.layoutNoResults);
        etSearch = findViewById(R.id.etSearch);

        rvMessagesList.setLayoutManager(new LinearLayoutManager(this));
        conversationList = new ArrayList<>();
        adapter = new MessagesListAdapter(this, displayList);
        rvMessagesList.setAdapter(adapter);

        setupSearch();
        loadConversations();
        loadConnectedUsers();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filter(String query) {
        if (query.trim().isEmpty()) {
            displayList.clear();
            displayList.addAll(conversationList);
            adapter.notifyDataSetChanged();
            updateEmptyState();
            layoutNoResults.setVisibility(View.GONE);
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        List<Object> filtered = new ArrayList<>();

        // 1. Filter existing conversations
        // We'll trust the adapter's cached names if possible, but here we might need to filter by participants we've resolved.
        // For simplicity, we compare with model's internal data or resolved names if we added them to Model.
        // Actually, let's keep it simple: filter existing conversation list by resolved names.
        // (Wait, Conversations don't store participant names. I resolved them in Adapter).
        // I'll need to improve the Model or resolve them here.
        
        // For now, let's filter conversations by chatId as a placeholder or by participants if they match the query.
        // Better: Search mutualFriends first, and find matching conversations for those friends.
        
        for (UserModel user : connectedUsers) {
            String fullName = user.getFull_name() != null ? user.getFull_name().toLowerCase() : "";
            String username = user.getUsername() != null ? user.getUsername().toLowerCase() : "";

            if (fullName.contains(lowerQuery) || username.contains(lowerQuery)) {
                
                // Check if we already have a conversation with this connected user
                boolean hasConv = false;
                for (ConversationModel conv : conversationList) {
                    if (conv.getParticipants() != null && conv.getParticipants().contains(user.getId())) {
                        if (!filtered.contains(conv)) filtered.add(conv);
                        hasConv = true;
                        break;
                    }
                }
                
                if (!hasConv) {
                    if (!filtered.contains(user)) filtered.add(user); // Add as "Start Chat" result
                }
            }
        }

        displayList.clear();
        displayList.addAll(filtered);
        adapter.notifyDataSetChanged();

        rvMessagesList.setVisibility(displayList.isEmpty() ? View.GONE : View.VISIBLE);
        layoutNoResults.setVisibility(displayList.isEmpty() ? View.VISIBLE : View.GONE);
        layoutEmptyChats.setVisibility(View.GONE);
    }

    private void loadConnectedUsers() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        
        // Clear previous state to avoid duplicates on reload
        connectedUserIds.clear();
        connectedUsers.clear();

        // 1. Fetch people I follow
        fStore.collection("Following").document(uid)
                .collection("UserFollowing")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot) {
                        String followedUid = doc.getId();
                        if (connectedUserIds.add(followedUid)) {
                            loadUserDetails(followedUid);
                        }
                    }
                });

        // 2. Fetch people who follow me
        fStore.collection("Followers").document(uid)
                .collection("UserFollowers")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot) {
                        String followerUid = doc.getId();
                        if (connectedUserIds.add(followerUid)) {
                            loadUserDetails(followerUid);
                        }
                    }
                });
    }

    private void loadUserDetails(String userId) {
        fStore.collection("Users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        UserModel user = userDoc.toObject(UserModel.class);
                        if (user != null) {
                            user.setId(userDoc.getId());
                            // Add if not already present (double check)
                            boolean exists = false;
                            for (UserModel u : connectedUsers) {
                                if (u.getId().equals(user.getId())) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                connectedUsers.add(user);
                            }
                        }
                    }
                });
    }

    private void loadConversations() {
        if (mAuth.getCurrentUser() == null) return;
        
        String uid = mAuth.getCurrentUser().getUid();

        conversationsListener = fStore.collection("Conversations")
                .whereArrayContains("participants", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Handle error (maybe index still building)
                        return;
                    }

                    if (value != null) {
                        conversationList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                            ConversationModel model = doc.toObject(ConversationModel.class);
                            if (model != null) {
                                conversationList.add(model);
                            }
                        }
                        
                        // Default view is conversations
                        if (etSearch.getText().toString().trim().isEmpty()) {
                            displayList.clear();
                            displayList.addAll(conversationList);
                            adapter.notifyDataSetChanged();
                            sortConversations();
                            updateEmptyState();
                        }
                    }
                });
    }

    private void sortConversations() {
        if (conversationList.size() <= 1) return;

        java.util.Collections.sort(conversationList, (c1, c2) -> {
            Object t1 = c1.getLastTimestamp();
            Object t2 = c2.getLastTimestamp();

            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;

            long m1 = getMillis(t1);
            long m2 = getMillis(t2);

            return Long.compare(m2, m1); // Descending
        });
        
        if (etSearch.getText().toString().trim().isEmpty()) {
            displayList.clear();
            displayList.addAll(conversationList);
            adapter.notifyDataSetChanged();
        }
    }

    private long getMillis(Object timestamp) {
        if (timestamp instanceof com.google.firebase.Timestamp) {
            return ((com.google.firebase.Timestamp) timestamp).toDate().getTime();
        } else if (timestamp instanceof Long) {
            return (Long) timestamp;
        } else if (timestamp instanceof java.util.Date) {
            return ((java.util.Date) timestamp).getTime();
        }
        return 0;
    }

    private void updateEmptyState() {
        if (etSearch.getText().toString().trim().isEmpty()) {
            if (conversationList.isEmpty()) {
                layoutEmptyChats.setVisibility(View.VISIBLE);
                rvMessagesList.setVisibility(View.GONE);
            } else {
                layoutEmptyChats.setVisibility(View.GONE);
                rvMessagesList.setVisibility(View.VISIBLE);
            }
            layoutNoResults.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (conversationsListener != null) {
            conversationsListener.remove();
        }
    }
}
