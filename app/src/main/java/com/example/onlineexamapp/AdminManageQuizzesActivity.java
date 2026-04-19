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

public class AdminManageQuizzesActivity extends AppCompatActivity {

    private RecyclerView rvQuizzes;
    private AdminDiscoveryAdapter adapter;
    private List<DiscoveryActivityModel> quizList;
    private FirebaseFirestore fStore;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshQuizzes;
    private View llEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_quizzes);
        // Immersive Status Bar Fix
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        fStore = FirebaseFirestore.getInstance();
        rvQuizzes = findViewById(R.id.rvQuizzes);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshQuizzes = findViewById(R.id.swipeRefreshQuizzes);
        llEmptyState = findViewById(R.id.llEmptyState);
        EditText etSearchQuiz = findViewById(R.id.etSearchQuiz);
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        quizList = new ArrayList<>();
        adapter = new AdminDiscoveryAdapter(quizList);
        rvQuizzes.setLayoutManager(new LinearLayoutManager(this));
        rvQuizzes.setAdapter(adapter);

        loadQuizzes();

        swipeRefreshQuizzes.setOnRefreshListener(this::loadQuizzes);

        etSearchQuiz.addTextChangedListener(new TextWatcher() {
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

    private void loadQuizzes() {
        if (!swipeRefreshQuizzes.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        fStore.collection("DiscoveryActivities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshQuizzes.setRefreshing(false);
                    
                    if (task.isSuccessful() && task.getResult() != null) {
                        quizList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DiscoveryActivityModel quiz = document.toObject(DiscoveryActivityModel.class);
                            quiz.setId(document.getId());
                            quizList.add(quiz);
                        }
                        adapter.updateList(quizList);
                        updateEmptyState();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Failed to load quizzes: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvQuizzes.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvQuizzes.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuizzes(); // Refresh list if coming back from an Edit
    }
}
