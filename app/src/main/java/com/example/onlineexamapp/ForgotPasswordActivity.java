package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends BaseActivity {

    private ImageView btnBack;
    private EditText etEmailForgot;
    private AppCompatButton btnContinueForgot;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        btnBack = findViewById(R.id.btnBack);
        etEmailForgot = findViewById(R.id.etEmailForgot);
        btnContinueForgot = findViewById(R.id.btnContinueForgot);

        mAuth = FirebaseAuth.getInstance();

        btnBack.setOnClickListener(v -> finish());

        btnContinueForgot.setOnClickListener(v -> {
            String email = etEmailForgot.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmailForgot.setError("Please enter your email");
                etEmailForgot.requestFocus();
                return;
            }

            btnContinueForgot.setEnabled(false);
            btnContinueForgot.setText("Sending...");

            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Password reset link sent to your email",
                                Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(ForgotPasswordActivity.this, MailSentActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        btnContinueForgot.setEnabled(true);
                        btnContinueForgot.setText("Continue");
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }
}
