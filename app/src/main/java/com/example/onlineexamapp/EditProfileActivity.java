package com.example.onlineexamapp;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivEditProfilePic, ivEditCover;
    private EditText etName, etUsername, etBio;
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    private String uid;
    
    private String base64Profile = null;
    private String base64Cover = null;
    private ProgressDialog progressDialog;

    private ActivityResultLauncher<String> pickDpLauncher;
    private ActivityResultLauncher<String> pickCoverLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        uid = mAuth.getUid();

        etName = findViewById(R.id.etEditName);
        etUsername = findViewById(R.id.etEditUsername);
        etBio = findViewById(R.id.etEditBio);
        ivEditProfilePic = findViewById(R.id.ivEditProfilePic);
        ivEditCover = findViewById(R.id.ivCover);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving changes...");
        progressDialog.setCancelable(false);

        loadCurrentData();
        setupLaunchers();
        setupClickListeners();
    }

    private void loadCurrentData() {
        if (uid == null) return;
        fStore.collection("Users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                etName.setText(doc.getString("full_name"));
                etUsername.setText(doc.getString("username"));
                etBio.setText(doc.getString("bio"));

                String pPic = doc.getString("profile_pic");
                String cPic = doc.getString("cover_pic");

                if (pPic != null && !pPic.isEmpty()) {
                    byte[] bytes = Base64.decode(pPic, Base64.DEFAULT);
                    Glide.with(this).load(bytes).placeholder(R.drawable.ic_user_placeholder).into(ivEditProfilePic);
                }
                if (cPic != null && !cPic.isEmpty()) {
                    byte[] bytes = Base64.decode(cPic, Base64.DEFAULT);
                    Glide.with(this).load(bytes).placeholder(R.drawable.cover_photo).into(ivEditCover);
                }
            }
        });
    }

    private void setupLaunchers() {
        pickDpLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                base64Profile = uriToBase64(uri);
                if (base64Profile != null) {
                    ivEditProfilePic.setImageURI(uri);
                }
            }
        });

        pickCoverLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                base64Cover = uriToBase64(uri);
                if (base64Cover != null) {
                    ivEditCover.setImageURI(uri);
                }
            }
        });
    }

    private String uriToBase64(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 400, 400, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            Toast.makeText(this, "Image processing failed", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.ivBackEditProfile).setOnClickListener(v -> finish());
        findViewById(R.id.tvChangePhoto).setOnClickListener(v -> pickDpLauncher.launch("image/*"));
        findViewById(R.id.tvChangeCover).setOnClickListener(v -> pickCoverLauncher.launch("image/*"));

        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> saveProfileChanges());
    }

    private void saveProfileChanges() {
        String name = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (name.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Name and Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        Map<String, Object> updates = new HashMap<>();
        updates.put("full_name", name);
        updates.put("username", username);
        updates.put("bio", bio);

        if (base64Profile != null) updates.put("profile_pic", base64Profile);
        if (base64Cover != null) updates.put("cover_pic", base64Cover);

        fStore.collection("Users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Profile Updated Successfully! ✅", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}