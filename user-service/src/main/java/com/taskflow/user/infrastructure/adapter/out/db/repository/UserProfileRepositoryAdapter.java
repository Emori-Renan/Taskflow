package com.taskflow.user.infrastructure.adapter.out.db.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.taskflow.user.application.port.out.UserProfileRepositoryPort;
import com.taskflow.user.domain.model.UserProfile;
import com.taskflow.user.infrastructure.adapter.out.db.entity.UserProfileEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserProfileRepositoryAdapter implements UserProfileRepositoryPort {

    private final SpringDataUserProfileRepository repository;

    @Override
    public Optional<UserProfile> findById(UUID id) {
        return repository.findById(id)
            .map(this::toDomain);
    }

    @Override
    public UserProfile save(UserProfile profile) {
        UserProfileEntity entity = new UserProfileEntity(
            profile.getUserId(),
            profile.getEmail(),
            profile.getDisplayName(),
            profile.getAvatarUrl()
        );
        UserProfileEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private UserProfile toDomain(UserProfileEntity entity) {
        UserProfile profile = new UserProfile(entity.getId(), entity.getEmail(), entity.getDisplayName());
        profile.updateProfile(entity.getDisplayName(), entity.getAvatarUrl());
        return profile;
    }
}
