package com.example.onlineexamapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class AdminBroadcastActivity extends AppCompatActivity {

    private EditText etTitle, etMessage;
    private AppCompatButton btnSend;
    private ProgressBar progressBar;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_broadcast);

        fStore = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.etBroadcastTitle);
        etMessage = findViewById(R.id.etBroadcastMessage);
        btnSend = findViewById(R.id.btnSendBroadcast);
        progressBar = findViewById(R.id.progressBar);
        View btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnSend.setOnClickListener(v -> validateAndSend());
    }

    private void validateAndSend() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please enter both title and message", Toast.LENGTH_SHORT).show();
            return;
        }

        sendBroadcast(title, message);
    }

    private void sendBroadcast(String title, String message) {
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);
        progressBar.setVisibility(View.VISIBLE);

        // Fetch all users to distribute the notification
        fStore.collection("Users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                Toast.makeText(this, "No users found to notify", Toast.LENGTH_SHORT).show();
                resetUI();
                return;
            }

            WriteBatch batch = fStore.batch();
            int count = 0;
            int totalUsers = queryDocumentSnapshots.size();

            for (com.google.firebase.firestore.DocumentSnapshot userDoc : queryDocumentSnapshots) {
                String userId = userDoc.getId();
                
                // 🔹 Create Notification Document for each user
                DocumentReference notifRef = fStore.collection("Notifications")
                        .document(userId)
                        .collection("UserNotifications")
                        .document();

                Map<String, Object> notifData = new HashMap<>();
                notifData.put("senderId", "system_admin");
                notifData.put("senderName", "MindSpace Admin");
                notifData.put("senderImage", ""); // System admin avatar placeholder
                notifData.put("title", title);
                notifData.put("message", message);
                notifData.put("type", "broadcast");
                notifData.put("timestamp", FieldValue.serverTimestamp());
                notifData.put("read", false);

                batch.set(notifRef, notifData);
                count++;

                // Firestore batch limit is 500 operations
                if (count >= 450) {
                    // This is a simplified version. For massive scale, use Cloud Functions.
                    break; 
                }
            }

            final int finalCount = count;
            batch.commit().addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Broadcast sent successfully to " + finalCount + " users", Toast.LENGTH_LONG).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Broadcast failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                resetUI();
            });

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            resetUI();
        });
    }

    private void resetUI() {
        btnSend.setEnabled(true);
        btnSend.setAlpha(1.0f);
        progressBar.setVisibility(View.GONE);
    }
}
