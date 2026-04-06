package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class DescribeWorkplaceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_describe_workplace);

        // Back button
        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Cards (Naya Account Banane wale)
        CardView cardSchool = findViewById(R.id.cardSchool);
        CardView cardHigherEducation = findViewById(R.id.cardHigherEducation);
        CardView cardTeams = findViewById(R.id.cardTeams);
        CardView cardBusiness = findViewById(R.id.cardBusiness);

        View.OnClickListener navigateToCreateAccount = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DescribeWorkplaceActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        };

        cardSchool.setOnClickListener(navigateToCreateAccount);
        cardHigherEducation.setOnClickListener(navigateToCreateAccount);
        cardTeams.setOnClickListener(navigateToCreateAccount);
        cardBusiness.setOnClickListener(navigateToCreateAccount);

        // ... (Pehle ka code cards wala) ...

        // 🚀 SKIP BUTTON JUMP (Wapas "Create Account" Form par bhejo) 🚀
        Button btnSkip = findViewById(R.id.btnSkipDescribeWorkplace);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 👇 CHANGE: Ab Skip dabane se "Create Account" form aayega 👇
                Intent intent = new Intent(DescribeWorkplaceActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });
    }
}