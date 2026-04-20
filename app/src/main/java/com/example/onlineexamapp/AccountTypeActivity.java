package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AccountTypeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_type);

        // Back button logic
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 1. Chaaron Cards ko code me link karna
        CardView cardPersonal = findViewById(R.id.cardPersonal);
        CardView cardTeacher = findViewById(R.id.cardTeacher);
        CardView cardStudent = findViewById(R.id.cardStudent);
        CardView cardProfessional = findViewById(R.id.cardProfessional);

        // 2. Click karne par Describe Workplace (Page 2) par bhejne ka logic
        View.OnClickListener nextScreenListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountTypeActivity.this, DescribeWorkplaceActivity.class);
                startActivity(intent);
            }
        };

        // 3. Logic ko chaaron cards par set karna
        cardPersonal.setOnClickListener(nextScreenListener);
        cardTeacher.setOnClickListener(nextScreenListener);
        cardStudent.setOnClickListener(nextScreenListener);
        cardProfessional.setOnClickListener(nextScreenListener);

        // 4. Skip Button ka code
        Button btnSkip = findViewById(R.id.btnSkip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountTypeActivity.this, DescribeWorkplaceActivity.class);
                startActivity(intent);
            }
        });
    }
}
