package com.example.onlineexamapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private TextView tvName, tvPoints, tvCoins, tvRank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // अपनी XML की ID यहाँ ध्यान से मिला लेना!
        tvName = findViewById(R.id.tvProfileName);
        tvPoints = findViewById(R.id.tvProfilePoints);
        tvCoins = findViewById(R.id.tvProfileCoins);
        tvRank = findViewById(R.id.tvProfileRank);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

            fStore.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // डेटाबेस से डेटा निकालो
                    String name = documentSnapshot.getString("username");
                    long points = documentSnapshot.getLong("points");
                    long coins = documentSnapshot.getLong("coins");
                    String rank = documentSnapshot.getString("rank");

                    // स्क्रीन पर सेट करो
                    tvName.setText(name);
                    tvPoints.setText(String.valueOf(points)); // "0" दिखाएगा
                    tvCoins.setText(String.valueOf(coins));   // "0" दिखाएगा
                    tvRank.setText(rank);                     // "--" दिखाएगा
                }
            }).addOnFailureListener(e -> Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show());
        }
    }
}