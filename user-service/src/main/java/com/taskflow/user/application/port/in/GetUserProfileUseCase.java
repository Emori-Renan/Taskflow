package com.taskflow.user.application.port.in;

import java.util.UUID;

import com.taskflow.user.domain.model.UserProfile;

public interface GetUserProfileUseCase {
    UserProfile getCurrentUser();
    UserProfile getById(UUID userId);
}
