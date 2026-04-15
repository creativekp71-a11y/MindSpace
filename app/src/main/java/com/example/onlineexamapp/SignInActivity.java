package com.example.onlineexamapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

public class SignInActivity extends AppCompatActivity {

    private static final String PREFS_AUTH = "auth_prefs";
    private static final String KEY_REMEMBERED_EMAIL = "remembered_email";

    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnSignIn;
    private CheckBox chkRememberMe;
    private View btnGoogleLogin;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    processGoogleSignIn(result.getData());
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Toast.makeText(this, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        googleSignInClient = SocialAuthHelper.createGoogleSignInClient(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        chkRememberMe = findViewById(R.id.chkRememberMe);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        View btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        bindRememberedEmail();

        if (btnSignIn != null) {
            btnSignIn.setOnClickListener(v -> attemptSignIn());
        }

        if (btnGoogleLogin != null) {
            btnGoogleLogin.setOnClickListener(v -> {
                if (googleSignInClient != null) {
                    googleSignInClient.signOut().addOnCompleteListener(task -> {
                        googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
                    });
                }
            });
        }

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }
    }

    private void processGoogleSignIn(Intent data) {
        SocialAuthHelper.handleGoogleSignInResult(this, data, mAuth, fStore, null, new SocialAuthHelper.Callback() {
            @Override
            public void onSuccess() {
                startActivity(new Intent(SignInActivity.this, MainHomeActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SignInActivity.this, message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete() {
                // Done
            }
        });
    }

    private void attemptSignIn() {
        clearErrors();

        String email = getText(etEmail);
        String password = getText(etPassword);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        btnSignIn.setEnabled(false);
        btnSignIn.setText("Signing in...");

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            btnSignIn.setEnabled(true);
            btnSignIn.setText("Sign in");

            if (task.isSuccessful()) {
                persistRememberedEmail(email);
                startActivity(new Intent(SignInActivity.this, MainHomeActivity.class));
                finish();
            } else {
                Toast.makeText(
                        SignInActivity.this,
                        task.getException() != null
                                ? task.getException().getMessage()
                                : "Login failed. Please try again.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void bindRememberedEmail() {
        SharedPreferences preferences = getSharedPreferences(PREFS_AUTH, MODE_PRIVATE);
        String rememberedEmail = preferences.getString(KEY_REMEMBERED_EMAIL, "");
        if (!rememberedEmail.isEmpty()) {
            etEmail.setText(rememberedEmail);
            chkRememberMe.setChecked(true);
        }
    }

    private void persistRememberedEmail(String email) {
        SharedPreferences preferences = getSharedPreferences(PREFS_AUTH, MODE_PRIVATE);
        if (chkRememberMe.isChecked()) {
            preferences.edit().putString(KEY_REMEMBERED_EMAIL, email).apply();
        } else {
            preferences.edit().remove(KEY_REMEMBERED_EMAIL).apply();
        }
    }

    private String getText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void clearErrors() {
        if (etEmail != null) etEmail.setError(null);
        if (etPassword != null) etPassword.setError(null);
    }
}