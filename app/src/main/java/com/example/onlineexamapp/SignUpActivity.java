package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        EditText etFullName = findViewById(R.id.etFullName);
        EditText etUsername = findViewById(R.id.etUsername);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            // --- Validations ---
            if (fullName.isEmpty()) {
                etFullName.setError("Full name is required");
                return;
            }
            if (username.length() < 3) {
                etUsername.setError("Username must be at least 3 characters");
                return;
            }
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email");
                return;
            }
            if (pass.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                return;
            }

            btnSignUp.setText("Creating Account...");
            btnSignUp.setEnabled(false);

            // 1. Firebase Auth में अकाउंट बनाओ
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = mAuth.getCurrentUser().getUid();

                    // 2. नए यूज़र का 'Fresh' डेटा तैयार करो (सब 0 पर सेट)
                    Map<String, Object> user = new HashMap<>();
                    user.put("full_name", fullName);
                    user.put("username", username);
                    user.put("email", email);
                    user.put("points", 0);    // 👈 नया यूज़र = 0 पॉइंट्स
                    user.put("coins", 0);     // 👈 नया यूज़र = 0 कॉइन्स
                    user.put("rank", "--");   // 👈 अभी कोई रैंक नहीं
                    user.put("profile_pic", "");
                    user.put("cover_pic", "");

                    // 3. Firestore में सेव करो
                    fStore.collection("Users").document(uid).set(user).addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful()) {
                            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, DashboardActivity.class));
                            finish();
                        }
                    });
                } else {
                    btnSignUp.setEnabled(true);
                    btnSignUp.setText("Sign Up");
                    Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}