package com.example.onlineexamapp;

import com.google.firebase.Timestamp;

public class NotificationModel {
    private String id;
    private String senderId;
    private String senderName;
    private String senderImage; // Base64
    private String title;
    private String message;
    private String type; // e.g., "new_discovery"
    private String activityId;
    private String chatId;
    private Timestamp timestamp;
    private boolean isRead;

    public NotificationModel() {
        // Required for Firestore
    }

    public NotificationModel(String senderId, String senderName, String senderImage, String title, String message, String type, String activityId, Timestamp timestamp) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderImage = senderImage;
        this.title = title;
        this.message = message;
        this.type = type;
        this.activityId = activityId;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getSenderImage() { return senderImage; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public String getActivityId() { return activityId; }
    public Timestamp getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
}
