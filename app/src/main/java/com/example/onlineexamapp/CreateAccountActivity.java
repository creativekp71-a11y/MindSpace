package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class CreateAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // ==========================================
        // 🔙 Back Button
        // ==========================================
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // Pichle page par wapas
                }
            });
        }

        // ==========================================
        // ➡️ Continue Button (Ye agle/final page par le jayega)
        // ==========================================
        Button btnContinue = findViewById(R.id.btnContinue);
        if (btnContinue != null) {
            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Ye FINAL SIGNUP page pe jayega
                    Intent intent = new Intent(CreateAccountActivity.this, SignUpActivity.class);
                    startActivity(intent);
                }
            });
        }

        // ==========================================
        // 🌐 Google Login बटन (अब ये Dashboard पर ले जाएगा)
        // ==========================================
        CardView btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        if (btnGoogleLogin != null) {
            btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // मस्त सा मैसेज दिखाओ
                    Toast.makeText(CreateAccountActivity.this, "Google से कनेक्ट हो रहा है... 🌐", Toast.LENGTH_SHORT).show();

                    // और सीधा Dashboard पर भेज दो
                    Intent intent = new Intent(CreateAccountActivity.this, DashboardActivity.class);
                    startActivity(intent);
                }
            });
        }

        // ==========================================
        // 📘 Facebook Login बटन (अब ये भी Dashboard पर ले जाएगा)
        // ==========================================
        CardView btnFacebookLogin = findViewById(R.id.btnFacebookLogin);
        if (btnFacebookLogin != null) {
            btnFacebookLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // मस्त सा मैसेज दिखाओ
                    Toast.makeText(CreateAccountActivity.this, "Facebook से कनेक्ट हो रहा है... 📘", Toast.LENGTH_SHORT).show();

                    // और सीधा Dashboard पर भेज दो
                    Intent intent = new Intent(CreateAccountActivity.this, DashboardActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}