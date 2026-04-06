package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class DiscoverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        // 1. Back button logic
        ImageView ivBack = findViewById(R.id.ivBackDiscover);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        // 2. Search icon logic
        ImageView ivSearch = findViewById(R.id.ivSearchDiscover);
        if (ivSearch != null) {
            ivSearch.setOnClickListener(v -> Toast.makeText(DiscoverActivity.this, "Opening Search...", Toast.LENGTH_SHORT).show());
        }

        // ==========================================
        // 👉 ASLI QUIZ KHOLNE KA JAADU 👈
        // ==========================================

        // ==========================================
        // 👉 CARD 1: Productivity Quiz 👈
        // ==========================================
        androidx.cardview.widget.CardView cardProductivity = findViewById(R.id.cardQuiz1); // अपनी XML वाली ID यहाँ डालना
        if (cardProductivity != null) {
            cardProductivity.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    android.content.Intent intent = new android.content.Intent(DiscoverActivity.this, QuizActivity.class);
                    intent.putExtra("QUIZ_CATEGORY", "Productivity"); // पार्सल भेजा
                    startActivity(intent);
                }
            });
        }

        // ==========================================
        // 👉 CARD 2: Brilliant Minds 👈
        // ==========================================
        androidx.cardview.widget.CardView cardBrilliant = findViewById(R.id.cardQuiz2); // अपनी XML वाली ID यहाँ डालना
        if (cardBrilliant != null) {
            cardBrilliant.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    android.content.Intent intent = new android.content.Intent(DiscoverActivity.this, QuizActivity.class);
                    intent.putExtra("QUIZ_CATEGORY", "Brilliant Minds"); // पार्सल भेजा
                    startActivity(intent);
                }
            });
        }

        // ==========================================
        // 👉 CARD 3: Having Fun 👈
        // ==========================================
        androidx.cardview.widget.CardView cardHavingFun = findViewById(R.id.cardQuiz3); // अपनी XML वाली ID यहाँ डालना
        if (cardHavingFun != null) {
            cardHavingFun.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    android.content.Intent intent = new android.content.Intent(DiscoverActivity.this, QuizActivity.class);
                    intent.putExtra("QUIZ_CATEGORY", "Having Fun"); // पार्सल भेजा
                    startActivity(intent);
                }
            });
        }
        // ==========================================
        // 👉 CARD 4: General Knowledge 👈
        // ==========================================
        androidx.cardview.widget.CardView cardGK = findViewById(R.id.cardQuiz4); // अपनी XML वाली ID यहाँ डालना
        if (cardGK != null) {
            cardGK.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    android.content.Intent intent = new android.content.Intent(DiscoverActivity.this, QuizActivity.class);
                    // 👉 पार्सल में "GK" नाम भेजा
                    intent.putExtra("QUIZ_CATEGORY", "General Knowledge");
                    startActivity(intent);
                }
            });
        }
        // ==========================================
        // 👉 CARD 5: Mathematics Quiz 👈
        // ==========================================
        androidx.cardview.widget.CardView cardMath = findViewById(R.id.cardQuiz5); // अपनी XML वाली ID यहाँ डालना
        if (cardMath != null) {
            cardMath.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    android.content.Intent intent = new android.content.Intent(DiscoverActivity.this, QuizActivity.class);
                    // 👉 पार्सल में "Mathematics" नाम भेजा
                    intent.putExtra("QUIZ_CATEGORY", "Mathematics");
                    startActivity(intent);
                }
            });
        }
        // ==========================================
        // 👉 CARD 6: Science Quiz 👈
        // ==========================================
        androidx.cardview.widget.CardView cardScience = findViewById(R.id.cardQuiz6); // अपनी XML वाली ID यहाँ डालना
        if (cardScience != null) {
            cardScience.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    android.content.Intent intent = new android.content.Intent(DiscoverActivity.this, QuizActivity.class);
                    // 👉 पार्सल में "Science" नाम भेजा
                    intent.putExtra("QUIZ_CATEGORY", "Science");
                    startActivity(intent);
                }
            });
        }

        // ==========================================
        // 👉 CARD 7: Geography Quiz 👈
        // ==========================================
        androidx.cardview.widget.CardView cardGeography = findViewById(R.id.cardQuiz7); // अपनी XML वाली ID यहाँ डालना
        if (cardGeography != null) {
            cardGeography.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    android.content.Intent intent = new android.content.Intent(DiscoverActivity.this, QuizActivity.class);
                    // 👉 पार्सल में "Geography" नाम भेजा
                    intent.putExtra("QUIZ_CATEGORY", "Geography");
                    startActivity(intent);
                }
            });
        }
    }
}