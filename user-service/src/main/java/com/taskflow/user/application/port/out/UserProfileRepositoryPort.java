package com.taskflow.user.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.taskflow.user.domain.model.UserProfile;

public interface UserProfileRepositoryPort {
    Optional<UserProfile> findById(UUID id);
    UserProfile save(UserProfile profile);
}
