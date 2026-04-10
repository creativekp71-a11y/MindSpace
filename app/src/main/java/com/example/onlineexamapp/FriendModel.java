package com.example.onlineexamapp;

public class FriendModel {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String profile_pic; // 👈 CHANGE HERE
    private boolean isFollowed;

    public FriendModel() {
    }

    public FriendModel(String userId, String name, String email, String phone, String profile_pic, boolean isFollowed) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profile_pic = profile_pic; // 👈 CHANGE
        this.isFollowed = isFollowed;
    }

    public String getUserId() {
        return userId == null ? "" : userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email == null ? "" : email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone == null ? "" : phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // 👇 IMPORTANT
    public String getProfileImage() {
        return profile_pic == null ? "" : profile_pic;
    }

    public void setProfileImage(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }
}