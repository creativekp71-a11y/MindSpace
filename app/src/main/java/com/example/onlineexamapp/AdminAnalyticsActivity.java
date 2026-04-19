package com.example.onlineexamapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdminAnalyticsActivity extends AppCompatActivity {

    private FirebaseFirestore fStore;
    private TextView tvTotalPlays, tvAvgScore, tvUniquePlayers;
    private LinearLayout llTopQuizzes, llTopAuthors;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshAnalytics;
    private ListenerRegistration attemptsListener, discoveriesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_analytics);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        fStore = FirebaseFirestore.getInstance();

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvTotalPlays = findViewById(R.id.tvTotalPlays);
        tvAvgScore = findViewById(R.id.tvAvgScore);
        tvUniquePlayers = findViewById(R.id.tvUniquePlayers);
        llTopQuizzes = findViewById(R.id.llTopQuizzes);
        llTopAuthors = findViewById(R.id.llTopAuthors);
        swipeRefreshAnalytics = findViewById(R.id.swipeRefreshAnalytics);

        if (swipeRefreshAnalytics != null) {
            swipeRefreshAnalytics.setOnRefreshListener(this::loadAnalytics);
            swipeRefreshAnalytics.setColorSchemeColors(android.graphics.Color.parseColor("#6C5CE7"));
        }

        loadAnalytics();
    }

    private void loadAnalytics() {
        if (attemptsListener != null) attemptsListener.remove();
        if (discoveriesListener != null) discoveriesListener.remove();

        if (swipeRefreshAnalytics != null) {
            swipeRefreshAnalytics.setRefreshing(true);
        }

        // 1. Live Quiz Attempts (For Intelligence)
        attemptsListener = fStore.collection("QuizAttempts").addSnapshotListener((snapshot, e) -> {
            if (snapshot != null) {
                List<QuizAttemptModel> attempts = new ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                    try {
                        String uid = doc.getString("userId");
                        String qTitle = doc.getString("quizTitle");
                        Long score = doc.getLong("score");
                        Long total = doc.getLong("totalQuestions");
                        
                        if (uid != null && qTitle != null && score != null && total != null) {
                            attempts.add(new QuizAttemptModel(uid, "User", doc.getString("quizId"), qTitle, score.intValue(), total.intValue()));
                        }
                    } catch (Exception ex) {
                        android.util.Log.e("Analytics", "Parsing error", ex);
                    }
                }
                calculateEngagementStats(attempts);
            }
            if (swipeRefreshAnalytics != null) swipeRefreshAnalytics.setRefreshing(false);
        });

        // 2. Live Discovery Activities (For Authors)
        discoveriesListener = fStore.collection("DiscoveryActivities").addSnapshotListener((snapshot, e) -> {
            if (snapshot != null) {
                calculateTopAuthors(snapshot.getDocuments());
            }
            if (swipeRefreshAnalytics != null) swipeRefreshAnalytics.setRefreshing(false);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (attemptsListener != null) attemptsListener.remove();
        if (discoveriesListener != null) discoveriesListener.remove();
    }

    private void calculateEngagementStats(List<QuizAttemptModel> attempts) {
        if (attempts == null || attempts.isEmpty()) {
            tvTotalPlays.setText("0");
            tvAvgScore.setText("0%");
            tvUniquePlayers.setText("0");
            renderTopQuizzes(new HashMap<>()); // Ensure placeholders are cleared
            return;
        }

        int totalPlays = attempts.size();
        int totalCorrect = 0;
        int totalQuestions = 0;
        Set<String> uniqueUsers = new HashSet<>();
        Map<String, Integer> quizTally = new HashMap<>();

        for (QuizAttemptModel attempt : attempts) {
            totalCorrect += attempt.getScore();
            totalQuestions += attempt.getTotalQuestions();
            uniqueUsers.add(attempt.getUserId());
            
            String quizTitle = attempt.getQuizTitle();
            quizTally.put(quizTitle, quizTally.getOrDefault(quizTitle, 0) + 1);
        }

        tvTotalPlays.setText(String.valueOf(totalPlays));
        tvUniquePlayers.setText(String.valueOf(uniqueUsers.size()));
        
        if (totalQuestions > 0) {
            int accuracy = (int) (((double) totalCorrect / totalQuestions) * 100);
            tvAvgScore.setText(accuracy + "%");
        } else {
            tvAvgScore.setText("0%");
        }

        renderTopQuizzes(quizTally);
    }

    private void renderTopQuizzes(Map<String, Integer> quizTally) {
        llTopQuizzes.removeAllViews();
        
        // Sort Map by value descending
        List<Map.Entry<String, Integer>> list = new ArrayList<>(quizTally.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        if (list.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No engagement data recorded yet.");
            tvEmpty.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tvEmpty.setPadding(20, 20, 20, 20);
            llTopQuizzes.addView(tvEmpty);
            return;
        }

        int count = 0;
        for (Map.Entry<String, Integer> entry : list) {
            if (count >= 5) break; 

            View itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, llTopQuizzes, false);
            TextView text1 = itemView.findViewById(android.R.id.text1);
            TextView text2 = itemView.findViewById(android.R.id.text2);

            text1.setText(entry.getKey());
            text1.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14);
            text1.setTextColor(getResources().getColor(android.R.color.black));
            
            text2.setText(entry.getValue() + " total attempts");
            text2.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12);

            llTopQuizzes.addView(itemView);
            count++;
        }
    }

    private void calculateTopAuthors(List<DocumentSnapshot> documents) {
        if (documents.isEmpty()) {
            llTopAuthors.removeAllViews();
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No contributors found.");
            tvEmpty.setPadding(20, 20, 20, 20);
            llTopAuthors.addView(tvEmpty);
            return;
        }

        Map<String, Integer> authorTally = new HashMap<>();
        for (DocumentSnapshot doc : documents) {
            String authorId = doc.getString("authorId");
            if (authorId != null) {
                authorTally.put(authorId, authorTally.getOrDefault(authorId, 0) + 1);
            }
        }

        // Sort Map by value descending
        List<Map.Entry<String, Integer>> list = new ArrayList<>(authorTally.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Resolve names for top 5 authors
        Map<String, String> resolvedNames = new HashMap<>();
        int limit = Math.min(list.size(), 5);
        final int[] fetchedCount = {0};

        if (limit == 0) {
            renderTopAuthors(authorTally, resolvedNames);
            return;
        }

        for (int i = 0; i < limit; i++) {
            String aid = list.get(i).getKey();
            fStore.collection("Users").document(aid).get().addOnSuccessListener(userDoc -> {
                String name = userDoc.getString("full_name");
                Boolean isAdmin = userDoc.getBoolean("isAdmin");
                
                // Skip System Admin or anyone with the Admin flag
                if ("System Admin".equalsIgnoreCase(name) || (isAdmin != null && isAdmin)) {
                    // Do not add to the resolved names map
                } else {
                    resolvedNames.put(aid, name != null ? name : "Anonymous");
                }
                
                fetchedCount[0]++;
                if (fetchedCount[0] == limit) {
                    renderTopAuthors(authorTally, resolvedNames);
                }
            }).addOnFailureListener(e -> {
                fetchedCount[0]++;
                if (fetchedCount[0] == limit) {
                    renderTopAuthors(authorTally, resolvedNames);
                }
            });
        }
    }

    private void renderTopAuthors(Map<String, Integer> authorTally, Map<String, String> authorNames) {
        llTopAuthors.removeAllViews();
        
        List<Map.Entry<String, Integer>> list = new ArrayList<>(authorTally.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int count = 0;
        for (Map.Entry<String, Integer> entry : list) {
            if (count >= 5) break;

            View itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, llTopAuthors, false);
            TextView text1 = itemView.findViewById(android.R.id.text1);
            TextView text2 = itemView.findViewById(android.R.id.text2);

            String name = authorNames.getOrDefault(entry.getKey(), "Author (ID: " + entry.getKey().substring(0, Math.min(entry.getKey().length(), 4)) + ")");
            text1.setText(name);
            text1.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14);
            text1.setTextColor(getResources().getColor(android.R.color.black));
            
            text2.setText(entry.getValue() + " quizzes published");
            text2.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12);
            text2.setTextColor(getResources().getColor(android.R.color.darker_gray));

            llTopAuthors.addView(itemView);
            count++;
        }
    }
}
