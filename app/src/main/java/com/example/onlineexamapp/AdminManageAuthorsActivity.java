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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminManageAuthorsActivity extends AppCompatActivity {

    private RecyclerView rvAuthors;
    private AdminAuthorAdapter adapter;
    private List<UserModel> authorList;
    private FirebaseFirestore fStore;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshAuthors;
    private View llEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_authors);

        fStore = FirebaseFirestore.getInstance();
        rvAuthors = findViewById(R.id.rvAuthors);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshAuthors = findViewById(R.id.swipeRefreshAuthors);
        llEmptyState = findViewById(R.id.llEmptyState);
        EditText etSearchAuthor = findViewById(R.id.etSearchAuthor);
        View btnBack = findViewById(R.id.btnBack);
        View ivLogout = findViewById(R.id.ivLogout);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (ivLogout != null) {
            ivLogout.setOnClickListener(v -> {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out from the Admin Center?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                            getSharedPreferences("auth_prefs", MODE_PRIVATE).edit().putBoolean("is_admin_logged_in", false).apply();
                            android.content.Intent intent = new android.content.Intent(this, SignInActivity.class);
                            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        authorList = new ArrayList<>();
        adapter = new AdminAuthorAdapter(authorList);
        rvAuthors.setLayoutManager(new LinearLayoutManager(this));
        rvAuthors.setAdapter(adapter);

        loadAuthors();

        swipeRefreshAuthors.setOnRefreshListener(this::loadAuthors);

        etSearchAuthor.addTextChangedListener(new TextWatcher() {
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

    private void loadAuthors() {
        if (!swipeRefreshAuthors.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        fStore.collection("Users")
                .whereEqualTo("isAuthor", true)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshAuthors.setRefreshing(false);
                    
                    if (task.isSuccessful() && task.getResult() != null) {
                        authorList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserModel user = document.toObject(UserModel.class);
                            user.setId(document.getId());
                            
                            // 🔹 Skip System Admin in the Author Management panel
                            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase("admin@mindspace.com")) {
                                continue;
                            }
                            
                            authorList.add(user);
                        }
                        adapter.updateList(authorList);
                        updateEmptyState();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Failed to load authors: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvAuthors.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvAuthors.setVisibility(View.VISIBLE);
        }
    }
}
