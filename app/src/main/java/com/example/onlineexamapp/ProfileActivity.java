package com.example.onlineexamapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.tasks.Task;
import com.bumptech.glide.Glide;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.app.ProgressDialog;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private FirebaseStorage fStorage;
    private TextView tvName, tvUsername, tvEmail, tvPoints, tvCoins, tvRank;
    private ImageView ivProfilePic, ivCover;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private String uploadType = ""; // "profile_pic" or "cover_pic"
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // अपनी XML की ID यहाँ ध्यान से मिला लेना!
        tvName = findViewById(R.id.tvProfileName);
        tvUsername = findViewById(R.id.tvProfileUsername);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvPoints = findViewById(R.id.tvProfilePoints);
        tvCoins = findViewById(R.id.tvProfileCoins);
        tvRank = findViewById(R.id.tvProfileRank);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        ivCover = findViewById(R.id.ivCover);

        // --- Back Button ---
        findViewById(R.id.ivBackProfile).setOnClickListener(v -> finish());

        // --- Menu Navigation ---
        findViewById(R.id.tvMenuAchievements).setOnClickListener(v -> 
            startActivity(new android.content.Intent(this, AchievementsActivity.class)));

        findViewById(R.id.tvMenuSettings).setOnClickListener(v -> 
            startActivity(new android.content.Intent(this, SettingsActivity.class)));

        findViewById(R.id.tvMenuShare).setOnClickListener(v -> {
            android.content.Intent sendIntent = new android.content.Intent();
            sendIntent.setAction(android.content.Intent.ACTION_SEND);
            sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hey! Check out MindSpace for awesome quizzes!");
            sendIntent.setType("text/plain");
            startActivity(android.content.Intent.createChooser(sendIntent, null));
        });

        fStorage = FirebaseStorage.getInstance("gs://mindspace-b98b4.appspot.com");

        // --- Image Picker Setup ---
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uploadImageToFirebase(uri);
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });

        // Click listeners for the new camera icons
        findViewById(R.id.btnEditProfilePic).setOnClickListener(v -> {
            uploadType = "profile_pic";
            imagePickerLauncher.launch("image/*");
        });

        findViewById(R.id.btnEditCover).setOnClickListener(v -> {
            uploadType = "cover_pic";
            imagePickerLauncher.launch("image/*");
        });

        // --- Logout Logic ---
        View btnLogout = findViewById(R.id.tvMenuLogout); // ID from activity_profile.xml
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                android.content.Intent intent = new android.content.Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void uploadImageToFirebase(Uri uri) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image");
        progressDialog.setMessage("Please wait while we update your profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String uid = mAuth.getCurrentUser().getUid();
        StorageReference ref = fStorage.getReference().child("Users/" + uid + "/" + uploadType + ".jpg");

        UploadTask uploadTask = ref.putFile(uri);

        // --- Canonical Way (Best Practice) ---
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                updateFirestore(downloadUri.toString());
            } else {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                String error = task.getException() != null ? task.getException().getMessage() : "Unknown Error";
                Log.e("FirebaseStorage", "Upload Task Failed: " + error);
                Toast.makeText(this, "Upload failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void updateFirestore(String url) {
        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> map = new HashMap<>();
        map.put(uploadType, url);

        fStore.collection("Users").document(uid).update(map).addOnSuccessListener(aVoid -> {
            if (progressDialog.isShowing()) progressDialog.dismiss();
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            
            // Load the new image immediately
            if (uploadType.equals("profile_pic")) {
                Glide.with(this).load(url).placeholder(R.drawable.ic_user_placeholder).error(R.drawable.ic_user_placeholder).into(ivProfilePic);
            } else {
                Glide.with(this).load(url).placeholder(R.drawable.cover_photo).error(R.drawable.cover_photo).into(ivCover);
            }
        }).addOnFailureListener(e -> {
            if (progressDialog.isShowing()) progressDialog.dismiss();
            Log.e("FirebaseFirestore", "Firestore Update Error: " + e.getMessage());
            Toast.makeText(this, "Database update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

            fStore.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // डेटाबेस से डेटा निकालो (Null checks added)
                    String fullName = documentSnapshot.getString("full_name");
                    String username = documentSnapshot.getString("username");
                    String email = documentSnapshot.getString("email");
                    
                    Long points = documentSnapshot.getLong("points");
                    Long coins = documentSnapshot.getLong("coins");
                    String rank = documentSnapshot.getString("rank");
                    String profilePic = documentSnapshot.getString("profile_pic");
                    String coverPic = documentSnapshot.getString("cover_pic");

                    // स्क्रीन पर सेट करो
                    tvName.setText(fullName != null ? fullName : "N/A");
                    tvUsername.setText(username != null ? "@" + username : "@username");
                    tvEmail.setText(email != null ? email : "No Email Found");
                    
                    tvPoints.setText(String.valueOf(points != null ? points : 0)); 
                    tvCoins.setText(String.valueOf(coins != null ? coins : 0));   
                    tvRank.setText(rank != null ? rank : "--");

                    // Load images with Glide
                    if (profilePic != null && !profilePic.isEmpty()) {
                        Glide.with(this).load(profilePic).placeholder(R.drawable.ic_user_placeholder).error(R.drawable.ic_user_placeholder).into(ivProfilePic);
                    }
                    if (coverPic != null && !coverPic.isEmpty()) {
                        Glide.with(this).load(coverPic).placeholder(R.drawable.cover_photo).error(R.drawable.cover_photo).into(ivCover);
                    }
                }
            }).addOnFailureListener(e -> Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show());
        }
    }
}