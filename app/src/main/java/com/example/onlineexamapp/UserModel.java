package com.example.onlineexamapp;

public class UserModel {
    private String id;
    private String full_name;
    private String username;
    private String email;
    private Long points;
    private Long coins;
    private String rank;
    private String profile_pic;
    private String cover_pic;
    private String bio;
    private Boolean isAuthor;
    private Boolean isBlocked;
    private Long followersCount;
    private Long followingCount;
    private Boolean isContentRestricted;

    public UserModel() {
        // Required for Firestore
    }

    public UserModel(String id, String full_name, String username, String email, Long points, Long coins, String rank, String profile_pic, String cover_pic, String bio, Boolean isAuthor, Boolean isBlocked, Long followersCount, Long followingCount, Boolean isContentRestricted) {
        this.id = id;
        this.full_name = full_name;
        this.username = username;
        this.email = email;
        this.points = points;
        this.coins = coins;
        this.rank = rank;
        this.profile_pic = profile_pic;
        this.cover_pic = cover_pic;
        this.bio = bio;
        this.isAuthor = isAuthor;
        this.isBlocked = isBlocked;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.isContentRestricted = isContentRestricted;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFull_name() { return full_name; }
    public void setFull_name(String full_name) { this.full_name = full_name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getPoints() { return points; }
    public void setPoints(Long points) { this.points = points; }

    public Long getCoins() { return coins; }
    public void setCoins(Long coins) { this.coins = coins; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    public String getProfile_pic() { return profile_pic; }
    public void setProfile_pic(String profile_pic) { this.profile_pic = profile_pic; }

    public String getCover_pic() { return cover_pic; }
    public void setCover_pic(String cover_pic) { this.cover_pic = cover_pic; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Boolean getIsAuthor() { return isAuthor; }
    public void setIsAuthor(Boolean author) { isAuthor = author; }

    public Long getFollowersCount() { return followersCount; }
    public void setFollowersCount(Long followersCount) { this.followersCount = followersCount; }

    public Long getFollowingCount() { return followingCount; }
    public void setFollowingCount(Long followingCount) { this.followingCount = followingCount; }

    public Boolean getIsBlocked() { return isBlocked != null && isBlocked; }
    public void setIsBlocked(Boolean blocked) { isBlocked = blocked; }

    public Boolean getIsContentRestricted() { return isContentRestricted != null && isContentRestricted; }
    public void setIsContentRestricted(Boolean contentRestricted) { isContentRestricted = contentRestricted; }
}
