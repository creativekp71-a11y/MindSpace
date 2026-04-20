package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class MailSentActivity extends BaseActivity {

    private ImageView btnBack;
    private TextView tvDescription;
    private AppCompatButton btnOpenEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_sent);

        btnBack = findViewById(R.id.btnBack);
        tvDescription = findViewById(R.id.tvDescription);
        btnOpenEmail = findViewById(R.id.btnOpenEmail);

        String email = getIntent().getStringExtra("email");

        if (email != null && !email.isEmpty()) {
            tvDescription.setText("We have sent a password reset link to " + email + ". Check your inbox and follow the link to create a new password.");
        } else {
            tvDescription.setText("We’ve sent a password reset link. Please check your inbox, promotions, or spam folder.");
        }

        btnBack.setOnClickListener(v -> finish());

        btnOpenEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }
}
