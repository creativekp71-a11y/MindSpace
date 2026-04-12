package com.example.onlineexamapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.ProgressDialog;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private TextView tvName, tvUsername, tvEmail, tvPoints, tvCoins, tvRank, tvMenuAddActivity, tvMenuFollowing;
    private TextView tvFollowersCount, tvFollowingCount;
    private View viewDividerAddActivity;
    private SwitchCompat switchBecomeAuthor;
    private ImageView ivProfilePic, ivCover;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private String uploadType = "";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        tvName = findViewById(R.id.tvProfileName);
        tvUsername = findViewById(R.id.tvProfileUsername);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvPoints = findViewById(R.id.tvProfilePoints);
        tvCoins = findViewById(R.id.tvProfileCoins);
        tvRank = findViewById(R.id.tvProfileRank);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        ivCover = findViewById(R.id.ivCover);
        tvFollowersCount = findViewById(R.id.tvProfileFollowersCount);
        tvFollowingCount = findViewById(R.id.tvProfileFollowingCount);
        tvMenuFollowing = findViewById(R.id.tvMenuFollowing);

        setupBottomNavigation();

        findViewById(R.id.ivBackProfile).setOnClickListener(v -> finish());

        findViewById(R.id.tvMenuAchievements).setOnClickListener(v ->
                startActivity(new Intent(this, AchievementsActivity.class)));

        findViewById(R.id.tvMenuSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        findViewById(R.id.tvMenuShare).setOnClickListener(v -> {
            String shareText = "📱 MindSpace Quiz App\n\n"
                    + "Hey! Try my app made for a college project.\n"
                    + "Download the APK from here:\n"
                    + "https://drive.google.com/file/d/1pwyLyBBJw3rjpffOcvoWWUUOkM1-IheX/view?usp=sharing"
                    + "Install it and enjoy the quiz app 🎯";

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "MindSpace Quiz App");
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(sendIntent, "Invite via"));
        });

        tvMenuFollowing.setOnClickListener(v ->
                startActivity(new Intent(this, FollowingListActivity.class)));

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uploadImageToFirebase(uri);
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnEditProfilePic).setOnClickListener(v -> {
            uploadType = "profile_pic";
            imagePickerLauncher.launch("image/*");
        });

        findViewById(R.id.btnEditCover).setOnClickListener(v -> {
            uploadType = "cover_pic";
            imagePickerLauncher.launch("image/*");
        });

        switchBecomeAuthor = findViewById(R.id.switchBecomeAuthor);
        tvMenuAddActivity = findViewById(R.id.tvMenuAddActivity);
        viewDividerAddActivity = findViewById(R.id.viewDividerAddActivity);

        switchBecomeAuthor.setOnCheckedChangeListener((buttonView, isChecked) -> updateAuthorStatus(isChecked));

        tvMenuAddActivity.setOnClickListener(v -> {
            startActivity(new Intent(this, AddDiscoveryActivity.class));
        });

        View btnLogout = findViewById(R.id.tvMenuLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing Image");
        progressDialog.setMessage("Optimizing and uploading directly to database...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (inputStream != null) inputStream.close();

                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                float ratio = (float) width / (float) height;
                int newWidth = 400;
                int newHeight = (int) (400 / ratio);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bos);
                byte[] bytes = bos.toByteArray();

                String base64String = Base64.encodeToString(bytes, Base64.DEFAULT);

                runOnUiThread(() -> updateFirestore(base64String));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    Log.e("Base64Encoding", "Error processing image: " + e.getMessage());
                    Toast.makeText(ProfileActivity.this, "Processing failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager connectivityManager =
                (android.net.ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.net.Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;
            android.net.NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                            || capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR));
        } else {
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    private void updateAuthorStatus(boolean isAuthor) {
        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> map = new HashMap<>();
        map.put("isAuthor", isAuthor);

        fStore.collection("Users").document(uid).update(map).addOnSuccessListener(aVoid -> {
            String msg = isAuthor ? "You are now an Author!" : "Author status removed.";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            updateAuthorUI(isAuthor);
        }).addOnFailureListener(e -> {
            switchBecomeAuthor.setChecked(!isAuthor);
            Toast.makeText(this, "Failed to update author status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateAuthorUI(boolean isAuthor) {
        if (isAuthor) {
            tvMenuAddActivity.setVisibility(View.VISIBLE);
            viewDividerAddActivity.setVisibility(View.VISIBLE);
        } else {
            tvMenuAddActivity.setVisibility(View.GONE);
            viewDividerAddActivity.setVisibility(View.GONE);
        }
    }

    private void updateFirestore(String base64) {
        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> map = new HashMap<>();
        map.put(uploadType, base64);

        fStore.collection("Users").document(uid).update(map).addOnSuccessListener(aVoid -> {
            if (progressDialog.isShowing()) progressDialog.dismiss();
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

            byte[] imageBytes = Base64.decode(base64, Base64.DEFAULT);
            if (uploadType.equals("profile_pic")) {
                Glide.with(this).load(imageBytes).placeholder(R.drawable.ic_user_placeholder).error(R.drawable.ic_user_placeholder).into(ivProfilePic);
            } else {
                Glide.with(this).load(imageBytes).placeholder(R.drawable.cover_photo).error(R.drawable.cover_photo).into(ivCover);
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
                    String fullName = documentSnapshot.getString("full_name");
                    String username = documentSnapshot.getString("username");
                    String email = documentSnapshot.getString("email");

                    Long points = documentSnapshot.getLong("points");
                    Long coins = documentSnapshot.getLong("coins");
                    String rank = documentSnapshot.getString("rank");
                    String profilePic = documentSnapshot.getString("profile_pic");
                    String coverPic = documentSnapshot.getString("cover_pic");

                    tvName.setText(fullName != null ? fullName : "N/A");
                    tvUsername.setText(username != null ? "@" + username : "@username");
                    tvEmail.setText(email != null ? email : "No Email Found");

                    tvPoints.setText(String.valueOf(points != null ? points : 0));
                    tvCoins.setText(String.valueOf(coins != null ? coins : 0));
                    tvRank.setText(rank != null ? rank : "--");

                    Long followers = documentSnapshot.getLong("followersCount");
                    Long following = documentSnapshot.getLong("followingCount");
                    tvFollowersCount.setText(String.valueOf(followers != null ? followers : 0));
                    tvFollowingCount.setText(String.valueOf(following != null ? following : 0));

                    Boolean isAuthor = documentSnapshot.getBoolean("isAuthor");
                    if (isAuthor == null) isAuthor = false;
                    switchBecomeAuthor.setChecked(isAuthor);
                    updateAuthorUI(isAuthor);

                    if (profilePic != null && !profilePic.isEmpty()) {
                        byte[] pBytes = Base64.decode(profilePic, Base64.DEFAULT);
                        Glide.with(this).load(pBytes).placeholder(R.drawable.ic_user_placeholder).error(R.drawable.ic_user_placeholder).into(ivProfilePic);
                    }
                    if (coverPic != null && !coverPic.isEmpty()) {
                        byte[] cBytes = Base64.decode(coverPic, Base64.DEFAULT);
                        Glide.with(this).load(cBytes).placeholder(R.drawable.cover_photo).error(R.drawable.cover_photo).into(ivCover);
                    }
                }
            }).addOnFailureListener(e -> Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, DashboardActivity.class));
            finish();
        });

        findViewById(R.id.navDiscover).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, DiscoverActivity.class));
            finish();
        });

        findViewById(R.id.navLeaderboard).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, LeaderboardActivity.class));
            finish();
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            // Already here
        });

        findViewById(R.id.ivCenterLogo).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, QuizActivity.class);
            intent.putExtra("QUIZ_CATEGORY", "Quick Play");
            startActivity(intent);
        });
    }
}