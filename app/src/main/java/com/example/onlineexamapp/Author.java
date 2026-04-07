package com.example.onlineexamapp;

public class Author {
    private String uid;
    private String fullName;
    private String username;
    private String profilePic;
    private boolean isAuthor;

    public Author() {
        // Required for Firestore
    }

    public Author(String uid, String fullName, String username, String profilePic, boolean isAuthor) {
        this.uid = uid;
        this.fullName = fullName;
        this.username = username;
        this.profilePic = profilePic;
        this.isAuthor = isAuthor;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

    public boolean isAuthor() { return isAuthor; }
    public void setAuthor(boolean author) { isAuthor = author; }
}
