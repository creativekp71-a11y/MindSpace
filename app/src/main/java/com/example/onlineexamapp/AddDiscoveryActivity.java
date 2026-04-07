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

public class AddDiscoveryActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etCategory;
    private TextView tvQuestionCount;
    private AppCompatButton btnAdd, btnAddQuestionDialog;
    private ImageView ivBack;
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    
    private List<Map<String, String>> questionList;

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

        ivBack.setOnClickListener(v -> finish());

        btnAddQuestionDialog.setOnClickListener(v -> showAddQuestionDialog());

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

    private void showAddQuestionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_question);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText etQ = dialog.findViewById(R.id.etQuestionText);
        EditText etA = dialog.findViewById(R.id.etOptionA);
        EditText etB = dialog.findViewById(R.id.etOptionB);
        EditText etC = dialog.findViewById(R.id.etOptionC);
        EditText etD = dialog.findViewById(R.id.etOptionD);
        EditText etCorrect = dialog.findViewById(R.id.etCorrectAnswer);
        AppCompatButton btnConfirm = dialog.findViewById(R.id.btnConfirmAddQuestion);

        btnConfirm.setOnClickListener(v -> {
            String q = etQ.getText().toString().trim();
            String a = etA.getText().toString().trim();
            String b = etB.getText().toString().trim();
            String c = etC.getText().toString().trim();
            String d = etD.getText().toString().trim();
            String correct = etCorrect.getText().toString().trim();

            if (TextUtils.isEmpty(q) || TextUtils.isEmpty(a) || TextUtils.isEmpty(b) || TextUtils.isEmpty(c) || TextUtils.isEmpty(d) || TextUtils.isEmpty(correct)) {
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

            questionList.add(questionData);
            tvQuestionCount.setText(questionList.size() + " added");
            dialog.dismiss();
            Toast.makeText(this, "Question added to list", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void saveToFirestore(String title, String desc, String category) {
        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("title", title);
        activityData.put("description", desc);
        activityData.put("category", category);
        activityData.put("authorId", uid);
        activityData.put("timestamp", com.google.firebase.Timestamp.now());
        activityData.put("questions", questionList);

        fStore.collection("DiscoveryActivities").add(activityData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Activity & Questions added successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add activity: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
