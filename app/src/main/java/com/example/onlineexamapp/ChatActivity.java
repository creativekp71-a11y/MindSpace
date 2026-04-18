package com.example.onlineexamapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String chatId, receiverId, receiverName;
    private String currentUserId;
    
    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageView ivSend;
    private CircleImageView imgHeader;
    private TextView tvHeaderName, tvHeaderUsername;
    private View layoutHeaderInfo;

    private ChatAdapter adapter;
    private List<ChatMessageModel> messageList;
    
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    private ListenerRegistration messagesListener;
    private ListenerRegistration followListener1, followListener2;
    private boolean followsMe = false, iFollowThem = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getStringExtra("chatId");
        receiverId = getIntent().getStringExtra("receiverId");
        receiverName = getIntent().getStringExtra("receiverName");

        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        if (chatId == null || receiverId == null) {
            finish();
            return;
        }

        initUI();
        loadMessages();
        setupFollowListeners();
    }

    private void initUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        imgHeader = findViewById(R.id.imgHeaderProfile);
        tvHeaderName = findViewById(R.id.tvHeaderName);
        tvHeaderUsername = findViewById(R.id.tvHeaderUsername);
        layoutHeaderInfo = findViewById(R.id.layoutHeaderInfo);
        
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        ivSend = findViewById(R.id.ivSend);

        tvHeaderName.setText(receiverName != null ? receiverName : "Chat");
        loadReceiverDetails();

        layoutHeaderInfo.setOnClickListener(v -> openReceiverProfile());
        imgHeader.setOnClickListener(v -> openReceiverProfile());

        rvChat.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        adapter = new ChatAdapter(this, messageList);
        rvChat.setAdapter(adapter);

        // Hide keyboard when tapping/scrolling the chat list
        rvChat.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                hideKeyboard();
            }
            return false;
        });

        ivSend.setOnClickListener(v -> sendMessage());
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void loadReceiverDetails() {
        fStore.collection("Users").document(receiverId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String fullName = doc.getString("full_name");
                String username = doc.getString("username");
                String profilePic = doc.getString("profile_pic");
                
                if (fullName != null) tvHeaderName.setText(fullName);
                if (username != null) {
                    tvHeaderUsername.setVisibility(View.VISIBLE);
                    tvHeaderUsername.setText("@" + username);
                } else {
                    tvHeaderUsername.setVisibility(View.GONE);
                }

                if (profilePic != null && !profilePic.isEmpty()) {
                    try {
                        byte[] bytes = Base64.decode(profilePic, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imgHeader.setImageBitmap(bitmap);
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    private void openReceiverProfile() {
        android.content.Intent intent = new android.content.Intent(this, AuthorProfileActivity.class);
        intent.putExtra("authorUid", receiverId);
        startActivity(intent);
    }

    private void loadMessages() {
        messagesListener = fStore.collection("Messages")
                .whereEqualTo("chatId", chatId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        messageList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                            ChatMessageModel model = doc.toObject(ChatMessageModel.class);
                            if (model != null) {
                                messageList.add(model);
                            }
                        }

                        // Manual sort to handle null timestamps (pending messages)
                        java.util.Collections.sort(messageList, (m1, m2) -> {
                            Object t1 = m1.getTimestamp();
                            Object t2 = m2.getTimestamp();
                            
                            if (t1 == null && t2 == null) return 0;
                            if (t1 == null) return 1; // Pending messages at the bottom
                            if (t2 == null) return -1;
                            
                            com.google.firebase.Timestamp ts1 = (com.google.firebase.Timestamp) t1;
                            com.google.firebase.Timestamp ts2 = (com.google.firebase.Timestamp) t2;
                            return ts1.compareTo(ts2);
                        });

                        adapter.notifyDataSetChanged();
                        if (adapter.getItemCount() > 0) {
                            rvChat.scrollToPosition(adapter.getItemCount() - 1);
                        }
                        markAsRead(); // Live read-sync
                    }
                });
    }

    private void setupFollowListeners() {
        // I follow them
        followListener1 = fStore.collection("Following").document(currentUserId)
                .collection("UserFollowing").document(receiverId)
                .addSnapshotListener((doc, e) -> {
                    iFollowThem = (doc != null && doc.exists());
                    updateLockedUI();
                });

        // They follow me
        followListener2 = fStore.collection("Following").document(receiverId)
                .collection("UserFollowing").document(currentUserId)
                .addSnapshotListener((doc, e) -> {
                    followsMe = (doc != null && doc.exists());
                    updateLockedUI();
                });
    }

    private void updateLockedUI() {
        View layoutInput = findViewById(R.id.layoutInput);
        View tvLockedChat = findViewById(R.id.tvLockedChat);
        
        if (iFollowThem && followsMe) {
            layoutInput.setVisibility(View.VISIBLE);
            tvLockedChat.setVisibility(View.GONE);
        } else {
            layoutInput.setVisibility(View.GONE);
            tvLockedChat.setVisibility(View.VISIBLE);
        }
    }

    private void markAsRead() {
        DocumentReference conversationRef = fStore.collection("Conversations").document(chatId);
        conversationRef.update("unreadCount." + currentUserId, 0);

        // Update seen status for all messages sent to current user in this chat
        fStore.collection("Messages")
                .whereEqualTo("chatId", chatId)
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("seen", false)
                .get()
                .addOnSuccessListener(value -> {
                    if (value != null && !value.isEmpty()) {
                        WriteBatch batch = fStore.batch();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                            batch.update(doc.getReference(), "seen", true);
                        }
                        batch.commit();
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        etMessage.setText("");

        DocumentReference msgRef = fStore.collection("Messages").document();
        String messageId = msgRef.getId();

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("messageId", messageId);
        messageData.put("chatId", chatId);
        messageData.put("senderId", currentUserId);
        messageData.put("receiverId", receiverId);
        messageData.put("messageText", text);
        messageData.put("timestamp", FieldValue.serverTimestamp());
        messageData.put("seen", false);

        // Use batch to ensure conversation update and message creation happen together
        WriteBatch batch = fStore.batch();
        batch.set(msgRef, messageData);

        DocumentReference chatRef = fStore.collection("Conversations").document(chatId);
        
        // Initialize conversation if it doesn't exist
        Map<String, Object> chatUpdate = new HashMap<>();
        chatUpdate.put("lastMessage", text);
        chatUpdate.put("lastTimestamp", FieldValue.serverTimestamp());
        chatUpdate.put("lastSenderId", currentUserId);
        chatUpdate.put("unreadCount." + receiverId, FieldValue.increment(1));
        chatUpdate.put("participants", Arrays.asList(currentUserId, receiverId));
        chatUpdate.put("chatId", chatId);

        batch.set(chatRef, chatUpdate, com.google.firebase.firestore.SetOptions.merge());

        batch.commit().addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        markAsRead();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
        if (followListener1 != null) {
            followListener1.remove();
        }
        if (followListener2 != null) {
            followListener2.remove();
        }
    }
}
