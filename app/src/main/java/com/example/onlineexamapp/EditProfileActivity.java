package com.example.onlineexamapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    // ==========================================
    // 1. क्लास लेवल वेरिएबल्स (Image Views और Launchers)
    // ==========================================
    private ImageView ivEditProfilePic;
    private ImageView ivEditCover;

    private ActivityResultLauncher<String> pickDpLauncher;
    private ActivityResultLauncher<String> pickCoverLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // ==========================================
        // 2. Views को XML से लिंक करना
        // ==========================================

        // Back Button
        ImageView ivBackEditProfile = findViewById(R.id.ivBackEditProfile);
        if (ivBackEditProfile != null) {
            ivBackEditProfile.setOnClickListener(v -> finish());
        }

        // Save Button
        androidx.appcompat.widget.AppCompatButton btnSaveProfile = findViewById(R.id.btnSaveProfile);
        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> {
                Toast.makeText(EditProfileActivity.this, "Profile Updated Successfully! ✅", Toast.LENGTH_SHORT).show();
                finish(); // सेव होने के बाद वापस मेन प्रोफाइल पर
            });
        }

        // Image Views (DP और Cover)
        // ध्यान दें: हमने कार्डव्यू के अंदर के ImageView को ढूँढा है
        androidx.cardview.widget.CardView cvProfile = findViewById(R.id.cvProfilePicCard);
        if(cvProfile != null) {
            // CardView के अंदर जो पहला (और इकलौता) ImageView है, उसे ले लो
            ivEditProfilePic = (ImageView) cvProfile.getChildAt(0);
        }

        androidx.cardview.widget.CardView cvCover = findViewById(R.id.cvCoverContainer);
        if(cvCover != null) {
            // CardView के अंदर जो पहला ImageView है, उसे ले लो
            ivEditCover = (ImageView) cvCover.getChildAt(0);
        }

        // ==========================================
        // 3. 'जादूई मशीनें' (Launchers) सेट करना
        // ==========================================

        // A. DP चुनने वाली मशीन
        pickDpLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri resultUri) {
                        if (resultUri != null && ivEditProfilePic != null) {
                            ivEditProfilePic.setImageURI(resultUri); // नई फोटो लगाओ
                            Toast.makeText(EditProfileActivity.this, "Profile Photo Selected! 🧑‍💼", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // B. Cover Photo चुनने वाली मशीन
        pickCoverLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri resultUri) {
                        if (resultUri != null && ivEditCover != null) {
                            ivEditCover.setImageURI(resultUri); // नया कवर लगाओ
                            Toast.makeText(EditProfileActivity.this, "Cover Photo Selected! 🏖️", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // ==========================================
        // 4. क्लिक लिसनर्स (बटन दबाने पर मशीन चलाना)
        // ==========================================

        // "Change Photo" (DP) बटन
        TextView tvChangePhoto = findViewById(R.id.tvChangePhoto);
        if (tvChangePhoto != null) {
            tvChangePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickDpLauncher.launch("image/*"); // गैलरी खोलो (सिर्फ इमेजेस के लिए)
                }
            });
        }

        // "Change Cover Photo" बटन
        TextView tvChangeCover = findViewById(R.id.tvChangeCover);
        if (tvChangeCover != null) {
            tvChangeCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickCoverLauncher.launch("image/*"); // गैलरी खोलो
                }
            });
        }
    }
}