package com.example.onlineexamapp;

public class Author {
    private String uid;
    private String fullName;
    private String username;
    private String profilePic;
    private String coverPic;
    private String bio;
    private boolean isAuthor;
    private long followersCount;
    private long followingCount;

    public Author() {
        // Required for Firestore
    }

    public Author(String uid, String fullName, String username, String profilePic, String coverPic, String bio, boolean isAuthor, long followersCount, long followingCount) {
        this.uid = uid;
        this.fullName = fullName;
        this.username = username;
        this.profilePic = profilePic;
        this.coverPic = coverPic;
        this.bio = bio;
        this.isAuthor = isAuthor;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

    public String getCoverPic() { return coverPic; }
    public void setCoverPic(String coverPic) { this.coverPic = coverPic; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public boolean isAuthor() { return isAuthor; }
    public void setAuthor(boolean author) { isAuthor = author; }

    public long getFollowersCount() { return followersCount; }
    public void setFollowersCount(long followersCount) { this.followersCount = followersCount; }

    public long getFollowingCount() { return followingCount; }
    public void setFollowingCount(long followingCount) { this.followingCount = followingCount; }
}
