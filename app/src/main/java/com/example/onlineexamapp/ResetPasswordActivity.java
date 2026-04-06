package com.example.onlineexamapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        final EditText etNewPass = findViewById(R.id.etNewPassword);
        final EditText etConfirmPass = findViewById(R.id.etConfirmPassword);
        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnContinue = findViewById(R.id.btnContinueReset);

        btnBack.setOnClickListener(v -> finish());

        // Eye Toggle Setup
        setupPasswordToggle(etNewPass);
        setupPasswordToggle(etConfirmPass);

        btnContinue.setOnClickListener(v -> showSuccessDialog());
    }

    private void setupPasswordToggle(final EditText editText) {
        editText.setOnTouchListener(new View.OnTouchListener() {
            boolean isVisible = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - 50)) {

                        if (isVisible) {
                            // Password Chhupao (Dots)
                            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed_slash, 0);
                            isVisible = false;
                        } else {
                            // Password Dikhao (Text)
                            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0);
                            isVisible = true;
                        }

                        // Cursor ko end mein le jao
                        editText.setSelection(editText.getText().length());
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void showSuccessDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_welcome_back);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // 👇 YE LINE MISSING THI 👇
        Button btnGoHome = dialog.findViewById(R.id.btnGoHome);

        btnGoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(ResetPasswordActivity.this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        dialog.show();
    }

}