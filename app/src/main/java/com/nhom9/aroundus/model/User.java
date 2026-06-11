package com.nhom9.aroundus.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String displayName;
    private String email;
    // Danh sách placeId mà người dùng đã lưu vào yêu thích
    private List<String> favoriteIds;

    public User() {}

    public User(String userId, String displayName, String email) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.favoriteIds = new ArrayList<>();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getFavoriteIds() {
        if (favoriteIds == null) favoriteIds = new ArrayList<>();
        return favoriteIds;
    }
    public void setFavoriteIds(List<String> favoriteIds) { this.favoriteIds = favoriteIds; }
}