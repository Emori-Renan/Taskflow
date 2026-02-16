package com.taskflow.user.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.taskflow.user.application.port.in.UpdateUserProfileUseCase;
import com.taskflow.user.application.port.out.UserProfileRepositoryPort;
import com.taskflow.user.domain.exception.UserNotFoundException;
import com.taskflow.user.domain.model.UserProfile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateUserProfileService implements UpdateUserProfileUseCase {

    private final UserProfileRepositoryPort repository;

    @Override
    public UserProfile update(UUID userId, String displayName, String avatarUrl) {
        UserProfile profile = repository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        profile.updateProfile(displayName, avatarUrl);
        return repository.save(profile);
    }
}
