package com.example.onlineexamapp;

import java.util.List;
import java.util.Map;

public class ConversationModel {
    private String chatId;
    private List<String> participants;
    private String lastMessage;
    private Object lastTimestamp; // Using Object to handle both Long and ServerTimestamp
    private String lastSenderId;
    private Map<String, Object> unreadCount;

    public ConversationModel() {
        // Required for Firestore
    }

    public ConversationModel(String chatId, List<String> participants, String lastMessage, Object lastTimestamp, String lastSenderId, Map<String, Object> unreadCount) {
        this.chatId = chatId;
        this.participants = participants;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
        this.lastSenderId = lastSenderId;
        this.unreadCount = unreadCount;
    }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public Object getLastTimestamp() { return lastTimestamp; }
    public void setLastTimestamp(Object lastTimestamp) { this.lastTimestamp = lastTimestamp; }

    public String getLastSenderId() { return lastSenderId; }
    public void setLastSenderId(String lastSenderId) { this.lastSenderId = lastSenderId; }

    public Map<String, Object> getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Map<String, Object> unreadCount) { this.unreadCount = unreadCount; }
}
