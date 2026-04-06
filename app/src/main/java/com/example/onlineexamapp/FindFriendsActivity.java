package com.example.onlineexamapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class FindFriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        // 1. Back button
        ImageView btnBack = findViewById(R.id.ivBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // 2. Search Contact Page
        RelativeLayout btnContactMenu = findViewById(R.id.btnContactMenu);
        if (btnContactMenu != null) {
            btnContactMenu.setOnClickListener(v -> startActivity(new Intent(FindFriendsActivity.this, ContactSearchActivity.class)));
        }

        // 3. Facebook Toast
        RelativeLayout btnFacebookMenu = findViewById(R.id.btnFacebookMenu);
        if (btnFacebookMenu != null) {
            btnFacebookMenu.setOnClickListener(v -> Toast.makeText(FindFriendsActivity.this, "Connecting to Facebook...", Toast.LENGTH_SHORT).show());
        }

        // 4. Invite Friends Share Menu
        RelativeLayout btnInviteMenu = findViewById(R.id.btnInviteMenu);
        if (btnInviteMenu != null) {
            btnInviteMenu.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Join me on MindSpace!");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Let's play quizzes together on MindSpace. Download the app now: https://play.google.com/store/apps/details?id=com.example.onlineexamapp");
                startActivity(Intent.createChooser(shareIntent, "Invite friends via"));
            });
        }

        // 5. Follow/Unfollow Swap Logic (Dono dabbe)
        LinearLayout layoutPYMK = findViewById(R.id.layoutPYMK);
        LinearLayout layoutSuggested = findViewById(R.id.layoutSuggested);

        int[] buttonIds = {
                R.id.btnFollow1, R.id.btnFollow2, R.id.btnFollow3, R.id.btnFollow4,
                R.id.btnFollow5, R.id.btnFollow6, R.id.btnFollow7, R.id.btnFollow8,
                R.id.btnFollow9, R.id.btnFollow10, R.id.btnFollow11, R.id.btnFollow12,
                R.id.btnFollow13, R.id.btnFollow14, R.id.btnFollow15, R.id.btnFollow16
        };

        for (int id : buttonIds) {
            AppCompatButton btn = findViewById(id);
            if (btn != null) {
                setupDynamicToggle(btn, layoutPYMK, layoutSuggested);
            }
        }

        // ==========================================
        // 6. 👉 MAIN SEARCH FILTER LOGIC 👈
        // ==========================================
        EditText etSearchFriends = findViewById(R.id.etSearchFriends);
        if (etSearchFriends != null) {
            etSearchFriends.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().toLowerCase().trim();

                    // Dono dabbon mein ek saath search karo
                    filterList(layoutPYMK, query);
                    filterList(layoutSuggested, query);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    // List ko filter (hide/show) karne wala function
    private void filterList(LinearLayout container, String query) {
        if (container == null) return;

        for (int i = 0; i < container.getChildCount(); i++) {
            View childRow = container.getChildAt(i);

            if (childRow instanceof RelativeLayout) {
                RelativeLayout row = (RelativeLayout) childRow;
                boolean matchFound = false;

                // Row ke andar naam dhoondho
                for (int j = 0; j < row.getChildCount(); j++) {
                    View element = row.getChildAt(j);

                    // Check karo ki ye TextView hai, par Button nahi
                    if (element instanceof TextView && !(element instanceof AppCompatButton)) {
                        String name = ((TextView) element).getText().toString().toLowerCase();
                        if (name.contains(query)) {
                            matchFound = true;
                            break;
                        }
                    }
                }

                // Agar query khali hai ya naam mil gaya, toh dikhao. Warna hide (gayab) kar do.
                if (matchFound || query.isEmpty()) {
                    childRow.setVisibility(View.VISIBLE);
                } else {
                    childRow.setVisibility(View.GONE);
                }
            }
        }
    }

    // Toggle (Swap) karne wala function
    private void setupDynamicToggle(final AppCompatButton button, final LinearLayout layoutPYMK, final LinearLayout layoutSuggested) {
        button.setOnClickListener(v -> {
            boolean isCurrentlyFollowing = button.getText().toString().equalsIgnoreCase("Unfollow");
            View personRow = (View) button.getParent();

            ViewGroup currentParent = (ViewGroup) personRow.getParent();
            if (currentParent != null) currentParent.removeView(personRow);

            button.setBackgroundTintList(null);

            if (isCurrentlyFollowing) {
                button.setText("Follow");
                button.setTextColor(Color.WHITE);

                GradientDrawable solidDesign = new GradientDrawable();
                solidDesign.setColor(Color.parseColor("#6C5CE7"));
                solidDesign.setCornerRadius(12f);
                button.setBackground(solidDesign);

                if (layoutSuggested != null) layoutSuggested.addView(personRow);

            } else {
                button.setText("Unfollow");
                button.setTextColor(Color.parseColor("#6C5CE7"));

                GradientDrawable outlineDesign = new GradientDrawable();
                outlineDesign.setColor(Color.WHITE);
                outlineDesign.setCornerRadius(12f);
                outlineDesign.setStroke(3, Color.parseColor("#6C5CE7"));
                button.setBackground(outlineDesign);

                if (layoutPYMK != null) layoutPYMK.addView(personRow);
            }
        });
    }
}