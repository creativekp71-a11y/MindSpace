package com.example.onlineexamapp;

public class ChatMessageModel {
    private String messageId;
    private String chatId;
    private String senderId;
    private String receiverId;
    private String messageText;
    private Object timestamp;
    private boolean seen;

    public ChatMessageModel() {
        // Required for Firestore
    }

    public ChatMessageModel(String messageId, String chatId, String senderId, String receiverId, String messageText, Object timestamp, boolean seen) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.seen = seen;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public Object getTimestamp() { return timestamp; }
    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }
}
