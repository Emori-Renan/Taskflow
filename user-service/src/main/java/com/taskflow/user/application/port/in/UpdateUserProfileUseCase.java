package com.taskflow.user.application.port.in;

import java.util.UUID;

import com.taskflow.user.domain.model.UserProfile;

public interface UpdateUserProfileUseCase {
    UserProfile update(UUID userId, String displayName, String avatarUrl);
}
