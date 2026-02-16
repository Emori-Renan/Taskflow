package com.taskflow.user.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.taskflow.user.application.port.in.GetUserProfileUseCase;
import com.taskflow.user.application.port.out.UserProfileRepositoryPort;
import com.taskflow.user.domain.exception.UserNotFoundException;
import com.taskflow.user.domain.model.UserProfile;
import com.taskflow.user.shared.security.JwtUserContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetUserProfileService implements GetUserProfileUseCase {

    private final UserProfileRepositoryPort repository;
    private final JwtUserContext userContext;

    @Override
    public UserProfile getCurrentUser() {
        UUID userId = userContext.userId();
        return repository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public UserProfile getById(UUID userId) {
        return repository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
