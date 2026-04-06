package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {

    private LinearLayout layoutDots;
    private ImageView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Note: Tera code activity_splash use kar raha hai, ensure tere dono buttons isi XML mein hon!
        setContentView(R.layout.activity_splash);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        layoutDots = findViewById(R.id.layoutDots); // Dots wala dabba dhoondha

        int[] sliderImages = {
                R.drawable.mindspace_intro,
                R.drawable.mindspace_intro_2,
                R.drawable.mindspace_intro_3
        };

        String[] sliderTexts = {
                "Create, share and play\nquizzes whenever and\nwherever you want",
                "Explore a world of knowledge\nacross diverse quiz categories.",
                "Monitor your progress,\nview results and climb the leaderboard!"
        };

        SliderAdapter adapter = new SliderAdapter(sliderImages, sliderTexts);
        viewPager.setAdapter(adapter);

        // 1. Dots ko screen par lagana (Total 3 dots)
        setupDots(sliderImages.length);

        // 2. Jab user swipe kare, toh dot ka color change karna
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(position, sliderImages.length);
            }
        });

        // =========================================================
        // Button 1: Let's Get Started (Naya Account)
        // =========================================================
        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AccountTypeActivity.class);
                startActivity(intent);
            }
        });

        // =========================================================
        // 👇👇 NAYA CODE ADD KIYA HAI (Sign In Jump) 👇👇
        // =========================================================
        // Button 2: I Already Have An Account
        Button btnAlreadyHaveAccount = findViewById(R.id.btnAlreadyAccount);
        // Agar btnAlreadyAccount null nahi hai tabhi click chale (safe code)
        if(btnAlreadyHaveAccount != null) {
            btnAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                    startActivity(intent);
                }
            });
        }
        // 👆👆 YAHAN TAK NAYA CODE HAI 👆👆
    }

    // Dots banane ka function
    private void setupDots(int count) {
        dots = new ImageView[count];
        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_inactive));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(12, 0, 12, 0); // Dots ke bich ka gap
            layoutDots.addView(dots[i], params);
        }
        // Pehle dot ko active (purple) kar do
        if (dots.length > 0) {
            dots[0].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_active));
        }
    }

    // Active dot ka color change karne ka function
    private void updateDots(int position, int count) {
        for (int i = 0; i < count; i++) {
            if (i == position) {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_active));
            } else {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_inactive));
            }
        }
    }
}