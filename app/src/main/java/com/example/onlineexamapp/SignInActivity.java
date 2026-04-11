package com.example.onlineexamapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private static final String PREFS_AUTH = "auth_prefs";
    private static final String KEY_REMEMBERED_EMAIL = "remembered_email";

    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnSignIn;
    private CheckBox chkRememberMe;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleAuthLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        googleSignInClient = SocialAuthHelper.createGoogleSignInClient(this);
        googleAuthLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() == null) {
                        return;
                    }

                    SocialAuthHelper.handleGoogleSignInResult(
                            this,
                            result.getData(),
                            mAuth,
                            fStore,
                            new HashMap<>(),
                            new SocialAuthHelper.Callback() {
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
                                }
                            }
                    );
                }
        );

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        chkRememberMe = findViewById(R.id.chkRememberMe);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        bindRememberedEmail();

        btnSignIn.setOnClickListener(v -> attemptSignIn());
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        CardView btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        CardView btnFacebookLogin = findViewById(R.id.btnFacebookLogin);
        btnGoogleLogin.setOnClickListener(v -> googleAuthLauncher.launch(googleSignInClient.getSignInIntent()));
        btnFacebookLogin.setOnClickListener(v -> showSocialAuthMessage("Facebook"));
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
                        task.getException() != null ? task.getException().getMessage() : "Login failed. Please try again.",
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

    private void showSocialAuthMessage(String provider) {
        Toast.makeText(
                this,
                provider + " sign-in is not configured in this build yet.",
                Toast.LENGTH_LONG
        ).show();
    }

    private String getText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void clearErrors() {
        etEmail.setError(null);
        etPassword.setError(null);
    }
}
