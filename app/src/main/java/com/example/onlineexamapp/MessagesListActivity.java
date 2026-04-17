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
    private LinearLayout layoutEmptyChats;
    private MessagesListAdapter adapter;
    private List<ConversationModel> conversationList;
    
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvMessagesList = findViewById(R.id.rvMessagesList);
        layoutEmptyChats = findViewById(R.id.layoutEmptyChats);

        rvMessagesList.setLayoutManager(new LinearLayoutManager(this));
        conversationList = new ArrayList<>();
        adapter = new MessagesListAdapter(this, conversationList);
        rvMessagesList.setAdapter(adapter);

        loadConversations();
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
                        adapter.notifyDataSetChanged();
                        
                        // Sort locally to handle missing timestamps gracefully
                        sortConversations();

                        updateEmptyState();
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
        adapter.notifyDataSetChanged();
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
        if (conversationList.isEmpty()) {
            layoutEmptyChats.setVisibility(View.VISIBLE);
            rvMessagesList.setVisibility(View.GONE);
        } else {
            layoutEmptyChats.setVisibility(View.GONE);
            rvMessagesList.setVisibility(View.VISIBLE);
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
