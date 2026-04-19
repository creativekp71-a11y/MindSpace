package com.example.onlineexamapp;

import com.google.firebase.Timestamp;

public class ReportModel {
    private String id;
    private String reporterId;
    private String reporterName;
    private String targetId;
    private String targetName; // Title of quiz or username
    private String targetType; // "content" or "user"
    private String reason;
    private String details;
    private String status; // "pending", "resolved", "dismissed"
    private Timestamp timestamp;

    public ReportModel() {}

    public ReportModel(String reporterId, String reporterName, String targetId, String targetName, String targetType, String reason, String details) {
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.targetId = targetId;
        this.targetName = targetName;
        this.targetType = targetType;
        this.reason = reason;
        this.details = details;
        this.status = "pending";
        this.timestamp = Timestamp.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReporterId() { return reporterId; }
    public String getReporterName() { return reporterName; }
    public String getTargetId() { return targetId; }
    public String getTargetName() { return targetName; }
    public String getTargetType() { return targetType; }
    public String getReason() { return reason; }
    public String getDetails() { return details; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getTimestamp() { return timestamp; }
}
