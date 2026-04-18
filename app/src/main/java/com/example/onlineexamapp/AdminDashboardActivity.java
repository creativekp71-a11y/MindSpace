package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        ImageView ivLogout = findViewById(R.id.ivLogout);
        View cardManageUsers = findViewById(R.id.cardManageUsers);

        if (ivLogout != null) {
            ivLogout.setOnClickListener(v -> {
                // Return to Sign In
                startActivity(new Intent(AdminDashboardActivity.this, SignInActivity.class));
                finish();
            });
        }

        if (cardManageUsers != null) {
            cardManageUsers.setOnClickListener(v -> {
                startActivity(new Intent(AdminDashboardActivity.this, AdminManageUsersActivity.class));
            });
        }

        // Toasts for unimplemented features
        findViewById(R.id.headerBg).setOnClickListener(v -> {}); // Prevent clicks through header
    }

    public void onBroadcastClick(View view) {
        Toast.makeText(this, "Broadcast feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    public void onReportsClick(View view) {
        Toast.makeText(this, "Reports feature coming soon!", Toast.LENGTH_SHORT).show();
    }
}
