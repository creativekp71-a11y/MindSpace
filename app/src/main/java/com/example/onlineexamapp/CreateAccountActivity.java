package com.example.onlineexamapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class CreateAccountActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;

    private EditText etFullName;
    private EditText tvDob;
    private EditText etPhone;
    private EditText etCountry;
    private EditText etAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        etFullName = findViewById(R.id.etFullName);
        tvDob = findViewById(R.id.tvDob);
        etPhone = findViewById(R.id.etPhone);
        etCountry = findViewById(R.id.etCountry);
        etAge = findViewById(R.id.etAge);
        Button btnContinue = findViewById(R.id.btnContinue);
        View btnBack = findViewById(R.id.btnBack);
        View btnCalendarIcon = findViewById(R.id.btnCalendarIcon);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCalendarIcon != null) {
            btnCalendarIcon.setOnClickListener(v -> showDatePicker());
        }

        if (tvDob != null) {
            tvDob.setOnClickListener(v -> showDatePicker());
        }

        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> continueToSignUp());
        }
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR) - 18;
        int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String dob = String.format(
                            java.util.Locale.getDefault(),
                            "%02d/%02d/%04d",
                            selectedDay,
                            selectedMonth + 1,
                            selectedYear
                    );

                    tvDob.setText(dob);

                    int age = calculateAge(selectedYear, selectedMonth, selectedDay);
                    etAge.setText(String.valueOf(age));
                },
                year,
                month,
                day
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private int calculateAge(int year, int month, int dayOfMonth) {
        Calendar dob = Calendar.getInstance();
        dob.set(year, month, dayOfMonth);

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return Math.max(age, 0);
    }

    private void continueToSignUp() {
        clearErrors();

        String fullName = getText(etFullName);
        String dob = getText(tvDob);
        String phone = getText(etPhone);
        String country = getText(etCountry);
        String age = getText(etAge);

        if (TextUtils.isEmpty(fullName) || fullName.length() < 3) {
            etFullName.setError("Enter your full name");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(dob)) {
            tvDob.setError("Select your date of birth");
            tvDob.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone) || !phone.matches("^[0-9]{10,15}$")) {
            etPhone.setError("Enter a valid phone number");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(country) || country.length() < 2) {
            etCountry.setError("Enter your country");
            etCountry.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(age)) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return;
        }

        int ageValue;
        try {
            ageValue = Integer.parseInt(age);
        } catch (NumberFormatException e) {
            etAge.setError("Enter a valid age");
            etAge.requestFocus();
            return;
        }

        if (ageValue < 13) {
            etAge.setError("You must be at least 13 years old");
            etAge.requestFocus();
            return;
        }

        Intent intent = new Intent(CreateAccountActivity.this, SignUpActivity.class);
        intent.putExtra(SignUpActivity.EXTRA_FULL_NAME, fullName);
        intent.putExtra(SignUpActivity.EXTRA_DOB, dob);
        intent.putExtra(SignUpActivity.EXTRA_PHONE, phone);
        intent.putExtra(SignUpActivity.EXTRA_COUNTRY, country);
        intent.putExtra(SignUpActivity.EXTRA_AGE, age);
        startActivity(intent);
    }

    private String getText(EditText editText) {
        return editText == null || editText.getText() == null
                ? ""
                : editText.getText().toString().trim();
    }

    private void clearErrors() {
        if (etFullName != null) etFullName.setError(null);
        if (tvDob != null) tvDob.setError(null);
        if (etPhone != null) etPhone.setError(null);
        if (etCountry != null) etCountry.setError(null);
        if (etAge != null) etAge.setError(null);
    }
}