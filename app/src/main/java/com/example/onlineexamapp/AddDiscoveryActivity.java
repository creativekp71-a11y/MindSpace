package com.example.onlineexamapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class AddDiscoveryActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etCategory;
    private TextView tvQuestionCount;
    private AppCompatButton btnAdd, btnAddQuestionDialog;
    private ImageView ivBack;
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    private ImageView ivCoverPreview;
    private String coverBase64 = "";
    private ActivityResultLauncher<String> imagePickerLauncher;
    
    private List<Map<String, String>> questionList;
    private RecyclerView rvQuestions;
    private QuestionPreviewAdapter questionAdapter;
    private boolean isEditMode = false;
    private String discoveryId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_discovery);

        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        questionList = new ArrayList<>();

        etTitle = findViewById(R.id.etDiscoveryTitle);
        etDescription = findViewById(R.id.etDiscoveryDescription);
        etCategory = findViewById(R.id.etDiscoveryCategory);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        btnAdd = findViewById(R.id.btnAddDiscovery);
        btnAddQuestionDialog = findViewById(R.id.btnAddQuestionDialog);
        ivBack = findViewById(R.id.ivBackAddDiscovery);
        ivCoverPreview = findViewById(R.id.ivDiscoveryCoverPreview);
        rvQuestions = findViewById(R.id.rvAddedQuestions);

        ivBack.setOnClickListener(v -> finish());

        // --- RecyclerView Setup ---
        questionAdapter = new QuestionPreviewAdapter(questionList, new QuestionPreviewAdapter.OnQuestionActionListener() {
            @Override
            public void onEdit(int position, Map<String, String> question) {
                showQuestionBottomSheet(position, question);
            }

            @Override
            public void onDelete(int position) {
                questionList.remove(position);
                questionAdapter.notifyItemRemoved(position);
                tvQuestionCount.setText(questionList.size() + " added");
            }
        });
        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvQuestions.setAdapter(questionAdapter);

        // --- Edit Mode Check ---
        isEditMode = getIntent().getBooleanExtra("IS_EDIT_MODE", false);
        if (isEditMode) {
            discoveryId = getIntent().getStringExtra("DISCOVERY_ID");
            TextView headerTitle = findViewById(R.id.tvAddDiscoveryHeaderTitle);
            if (headerTitle != null) {
                headerTitle.setText("Edit Discovery Activity");
            }
            btnAdd.setText("Update Activity");
            fetchDiscoveryData();
        }

        // --- Image Picker Setup ---
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                processImage(uri);
            }
        });

        findViewById(R.id.btnChangeDiscoveryCover).setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        btnAddQuestionDialog.setOnClickListener(v -> showQuestionBottomSheet(-1, null));

        btnAdd.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            String cat = etCategory.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(cat)) {
                Toast.makeText(this, "Please fill Title, Description and Category", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (questionList.isEmpty()) {
                Toast.makeText(this, "Please add at least one question", Toast.LENGTH_SHORT).show();
                return;
            }

            saveToFirestore(title, desc, cat);
        });
    }

    private void fetchDiscoveryData() {
        if (discoveryId == null) return;
        fStore.collection("DiscoveryActivities").document(discoveryId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        DiscoveryActivityModel model = doc.toObject(DiscoveryActivityModel.class);
                        if (model == null) return;

                        etTitle.setText(model.getTitle());
                        etDescription.setText(model.getDescription());
                        etCategory.setText(model.getCategory());
                        coverBase64 = model.getCover_pic();
                        
                        if (coverBase64 != null && !coverBase64.isEmpty()) {
                            try {
                                byte[] bytes = Base64.decode(coverBase64, Base64.DEFAULT);
                                Glide.with(this).load(bytes).into(ivCoverPreview);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        List<Map<String, String>> questions = model.getQuestions();
                        if (questions != null) {
                            questionList.clear();
                            questionList.addAll(questions);
                            questionAdapter.notifyDataSetChanged();
                            tvQuestionCount.setText(questionList.size() + " added");
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showQuestionBottomSheet(int position, Map<String, String> existingQuestion) {
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_add_question, null);
        if (view == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);

        // 🔥 Keyboard Issue Fix: Ensure BottomSheet adjusts when keyboard opens
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText etQ = view.findViewById(R.id.etQuestionText);
        EditText etA = view.findViewById(R.id.etOptionA);
        EditText etB = view.findViewById(R.id.etOptionB);
        EditText etC = view.findViewById(R.id.etOptionC);
        EditText etD = view.findViewById(R.id.etOptionD);
        EditText etCorrect = view.findViewById(R.id.etCorrectAnswer);
        TextView tvTitle = view.findViewById(R.id.tvBottomSheetTitle);
        AppCompatButton btnConfirm = view.findViewById(R.id.btnConfirmAddQuestion);

        if (existingQuestion != null) {
            if (tvTitle != null) tvTitle.setText("Edit Question");
            if (etQ != null) etQ.setText(existingQuestion.get("question"));
            if (etA != null) etA.setText(existingQuestion.get("optionA"));
            if (etB != null) etB.setText(existingQuestion.get("optionB"));
            if (etC != null) etC.setText(existingQuestion.get("optionC"));
            if (etD != null) etD.setText(existingQuestion.get("optionD"));
            if (etCorrect != null) etCorrect.setText(existingQuestion.get("correctAnswer"));
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                if (etQ == null || etA == null || etB == null || etC == null || etD == null || etCorrect == null) return;

                String q = etQ.getText().toString().trim();
                String a = etA.getText().toString().trim();
                String b = etB.getText().toString().trim();
                String c = etC.getText().toString().trim();
                String d = etD.getText().toString().trim();
                String correct = etCorrect.getText().toString().trim();

                if (TextUtils.isEmpty(q) || TextUtils.isEmpty(a) || TextUtils.isEmpty(b) ||
                        TextUtils.isEmpty(c) || TextUtils.isEmpty(d) || TextUtils.isEmpty(correct)) {

                    Toast.makeText(this, "Fill all question details", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, String> questionData = new HashMap<>();
                questionData.put("question", q);
                questionData.put("optionA", a);
                questionData.put("optionB", b);
                questionData.put("optionC", c);
                questionData.put("optionD", d);
                questionData.put("correctAnswer", correct);

                if (position == -1) {
                    questionList.add(questionData);
                    questionAdapter.notifyItemInserted(questionList.size() - 1);
                } else {
                    questionList.set(position, questionData);
                    questionAdapter.notifyItemChanged(position);
                }
                
                tvQuestionCount.setText(questionList.size() + " added");
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void processImage(Uri uri) {
        new Thread(() -> {
            try {
                // 🔹 Step 1: Get image dimensions first (to avoid OOM)
                InputStream inputDimensions = getContentResolver().openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputDimensions, null, options);
                if (inputDimensions != null) inputDimensions.close();

                // 🔹 Step 2: Calculate sample size
                options.inSampleSize = calculateInSampleSize(options, 800, 800);
                options.inJustDecodeBounds = false;

                // 🔹 Step 3: Decode with sample size
                InputStream inputActual = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputActual, null, options);
                if (inputActual != null) inputActual.close();

                if (bitmap == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to decode image", Toast.LENGTH_SHORT).show());
                    return;
                }

                // 🔹 Step 4: Scale down precisely
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                float ratio = (float) width / (float) height;
                int newWidth = 400; // Slightly smaller to be safe
                int newHeight = (int) (400 / ratio);
                if (newHeight <= 0) newHeight = 1;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                // 🔹 Step 5: Compress and Encode
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bos);
                byte[] bytes = bos.toByteArray();
                coverBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP); // NO_WRAP is better for JSON/Firestore

                runOnUiThread(() -> {
                    Glide.with(this).load(bytes).into(ivCoverPreview);
                    Toast.makeText(this, "Cover photo ready!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void saveToFirestore(String title, String desc, String category) {
        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("title", title);
        activityData.put("description", desc);
        activityData.put("category", category);
        activityData.put("authorId", uid);
        activityData.put("cover_pic", coverBase64); // 🔹 Adding the cover photo
        activityData.put("timestamp", com.google.firebase.Timestamp.now());
        activityData.put("questions", questionList);

        if (isEditMode) {
            fStore.collection("DiscoveryActivities").document(discoveryId).set(activityData, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Activity updated successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            fStore.collection("DiscoveryActivities").add(activityData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Activity added successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
