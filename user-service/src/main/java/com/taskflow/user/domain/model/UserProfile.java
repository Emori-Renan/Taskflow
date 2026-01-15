package com.taskflow.user.domain.model;

import java.util.UUID;

public class UserProfile {

    private final UUID userId;
    private String email;
    private String displayName;
    private String avatarUrl;

    public UserProfile(UUID userId, String email, String displayName) {
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email cannot be blank");
        
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
    }

    public UserProfile(UUID userId, String email) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email cannot be null or blank");
        }

        this.userId = userId;
        this.email = email;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    // Business behavior
    public void updateProfile(String displayName, String avatarUrl) {
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }
}
