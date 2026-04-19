package com.example.onlineexamapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminManageUsersActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private AdminUserAdapter adapter;
    private List<UserModel> userList;
    private FirebaseFirestore fStore;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshUsers;
    private View llEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_users);
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#6C5CE7"));
        getWindow().getDecorView().setSystemUiVisibility(0); // White icons

        fStore = FirebaseFirestore.getInstance();
        rvUsers = findViewById(R.id.rvUsers);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshUsers = findViewById(R.id.swipeRefreshUsers);
        llEmptyState = findViewById(R.id.llEmptyState);
        EditText etSearchUser = findViewById(R.id.etSearchUser);
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        userList = new ArrayList<>();
        adapter = new AdminUserAdapter(userList);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        loadUsers();

        swipeRefreshUsers.setOnRefreshListener(this::loadUsers);

        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        if (!swipeRefreshUsers.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        fStore.collection("Users")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshUsers.setRefreshing(false);
                    
                    if (task.isSuccessful() && task.getResult() != null) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserModel user = document.toObject(UserModel.class);
                            user.setId(document.getId());
                            
                            // 🔹 Skip System Admin in the Manage Users panel
                            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase("admin@mindspace.com")) {
                                continue;
                            }
                            
                            userList.add(user);
                        }
                        adapter.updateList(userList);
                        updateEmptyState();
                    } else {
                        String errorMessage = (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                        if (errorMessage != null && (errorMessage.contains("PERMISSION_DENIED") || errorMessage.contains("Access Denied"))) {
                            errorMessage = "Access Denied (UID: " + (uid != null ? uid : "NULL") + "). Your Firestore rules are blocking this request.";
                        }
                        Toast.makeText(this, "Failed to load users: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvUsers.setVisibility(View.VISIBLE);
        }
    }
}
