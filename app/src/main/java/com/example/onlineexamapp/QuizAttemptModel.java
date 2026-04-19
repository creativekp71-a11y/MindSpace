package com.example.onlineexamapp;

import com.google.firebase.Timestamp;

public class QuizAttemptModel {
    private String attemptId;
    private String userId;
    private String userName;
    private String quizId;
    private String quizTitle;
    private int score;
    private int totalQuestions;
    private Timestamp timestamp;

    public QuizAttemptModel() {}

    public QuizAttemptModel(String userId, String userName, String quizId, String quizTitle, int score, int totalQuestions) {
        this.userId = userId;
        this.userName = userName;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.timestamp = Timestamp.now();
    }

    // Getters and Setters
    public String getAttemptId() { return attemptId; }
    public void setAttemptId(String attemptId) { this.attemptId = attemptId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }

    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public com.google.firebase.Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(com.google.firebase.Timestamp timestamp) { this.timestamp = timestamp; }
}
