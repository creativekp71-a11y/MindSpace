package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    public static final String EXTRA_FULL_NAME = "extra_full_name";
    public static final String EXTRA_DOB = "extra_dob";
    public static final String EXTRA_PHONE = "extra_phone";
    public static final String EXTRA_COUNTRY = "extra_country";
    public static final String EXTRA_AGE = "extra_age";

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
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        CardView btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        CardView btnFacebookLogin = findViewById(R.id.btnFacebookLogin);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String prefilledFullName = getIntent().getStringExtra(EXTRA_FULL_NAME);
        if (prefilledFullName != null && !prefilledFullName.trim().isEmpty()) {
            etFullName.setText(prefilledFullName);
        }

        btnGoogleLogin.setOnClickListener(v -> showSocialAuthMessage("Google"));
        btnFacebookLogin.setOnClickListener(v -> showSocialAuthMessage("Facebook"));

        btnSignUp.setOnClickListener(v -> {
            clearErrors(etFullName, etUsername, etEmail, etPassword, etConfirmPassword);

            String fullName = getText(etFullName);
            String username = getText(etUsername);
            String email = getText(etEmail);
            String password = getText(etPassword);
            String confirmPassword = getText(etConfirmPassword);
            String dob = getIntent().getStringExtra(EXTRA_DOB);
            String phone = getIntent().getStringExtra(EXTRA_PHONE);
            String country = getIntent().getStringExtra(EXTRA_COUNTRY);
            String age = getIntent().getStringExtra(EXTRA_AGE);

            if (TextUtils.isEmpty(fullName) || fullName.length() < 3) {
                etFullName.setError("Enter your full name");
                etFullName.requestFocus();
                return;
            }

            if (!username.matches("^[a-zA-Z0-9._]{3,20}$")) {
                etUsername.setError("Use 3-20 letters, numbers, dot or underscore");
                etUsername.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email");
                etEmail.requestFocus();
                return;
            }

            if (!isStrongPassword(password)) {
                etPassword.setError("Use at least 8 characters with letters and numbers");
                etPassword.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }

            btnSignUp.setEnabled(false);
            btnSignUp.setText("Create account...");

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                    String uid = mAuth.getCurrentUser().getUid();

                    Map<String, Object> user = new HashMap<>();
                    user.put("full_name", fullName);
                    user.put("username", username);
                    user.put("email", email);
                    user.put("phone", phone == null ? "" : phone);
                    user.put("dob", dob == null ? "" : dob);
                    user.put("country", country == null ? "" : country);
                    user.put("age", age == null ? "" : age);
                    user.put("points", 0);
                    user.put("coins", 0);
                    user.put("rank", "--");
                    user.put("profile_pic", "");
                    user.put("cover_pic", "");
                    user.put("bio", "");
                    user.put("isAuthor", false);
                    user.put("followersCount", 0);
                    user.put("followingCount", 0);

                    fStore.collection("Users").document(uid).set(user).addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful()) {
                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainHomeActivity.class));
                            finish();
                        } else {
                            btnSignUp.setEnabled(true);
                            btnSignUp.setText("Create account");
                            Toast.makeText(this,
                                    dbTask.getException() != null ? dbTask.getException().getMessage() : "Failed to save account details.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    btnSignUp.setEnabled(true);
                    btnSignUp.setText("Create account");
                    Toast.makeText(this,
                            task.getException() != null ? task.getException().getMessage() : "Unable to create account.",
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private boolean isStrongPassword(String password) {
        return password != null
                && password.length() >= 8
                && password.matches(".*[A-Za-z].*")
                && password.matches(".*\\d.*");
    }

    private String getText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void clearErrors(EditText... editTexts) {
        for (EditText editText : editTexts) {
            editText.setError(null);
        }
    }

    private void showSocialAuthMessage(String provider) {
        Toast.makeText(this, provider + " sign-up is not configured in this build yet.", Toast.LENGTH_LONG).show();
    }
}
