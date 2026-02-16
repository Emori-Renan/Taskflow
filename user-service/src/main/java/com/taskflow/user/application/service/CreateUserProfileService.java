package com.taskflow.user.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.taskflow.user.application.port.in.CreateUserProfileUseCase;
import com.taskflow.user.application.port.out.UserProfileRepositoryPort;
import com.taskflow.user.domain.model.UserProfile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateUserProfileService implements CreateUserProfileUseCase {

    private final UserProfileRepositoryPort repository;

    @Override
    public UserProfile create(UUID userId, String email) {
        UserProfile profile = new UserProfile(userId, email);
        return repository.save(profile);
    }
}
