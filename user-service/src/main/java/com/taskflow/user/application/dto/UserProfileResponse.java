package com.taskflow.user.application.dto;

import com.taskflow.user.domain.model.UserProfile;
import java.util.UUID;

public record UserProfileResponse(
    UUID id,
    String email,
    String displayName
) {
    public static UserProfileResponse from(UserProfile profile) {
        return new UserProfileResponse(
            profile.getUserId(),
            profile.getEmail(),
            profile.getDisplayName()
        );
    }
}