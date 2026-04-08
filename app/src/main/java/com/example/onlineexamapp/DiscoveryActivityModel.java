package com.example.onlineexamapp;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

public class DiscoveryActivityModel {

    private String id;
    private String title;
    private String description;
    private String category;
    private String authorId;
    private String cover_pic; // 🔹 Base64 Thumbnail for the activity
    private Timestamp timestamp;
    private List<Map<String, String>> questions;

    // 🔹 Empty constructor (Firestore ke liye required)
    public DiscoveryActivityModel() {
    }

    // 🔹 Full Constructor (optional but useful)
    public DiscoveryActivityModel(String id, String title, String description,
                                  String category, String authorId, String cover_pic,
                                  Timestamp timestamp,
                                  List<Map<String, String>> questions) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.authorId = authorId;
        this.cover_pic = cover_pic;
        this.timestamp = timestamp;
        this.questions = questions;
    }

    // 🔹 Getters & Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getCover_pic() {
        return cover_pic;
    }

    public void setCover_pic(String cover_pic) {
        this.cover_pic = cover_pic;
    }

    public List<Map<String, String>> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Map<String, String>> questions) {
        this.questions = questions;
    }
}