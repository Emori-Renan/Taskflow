package com.taskflow.user.infrastructure.adapter.out.db.repository;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.taskflow.user.application.port.out.UserProfileRepositoryPort;
import com.taskflow.user.domain.model.UserProfile;
import com.taskflow.user.infrastructure.adapter.out.db.entity.UserProfileEntity;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserProfileRepositoryAdapter implements UserProfileRepositoryPort {

    private final SpringDataUserProfileRepository repository;

    @Override
    public Mono<UserProfile> findById(UUID id) {
        return repository.findById(id)
            .map(e -> new UserProfile(e.getId(), e.getEmail(), e.getDisplayName()));
    }

    @Override
    public Mono<UserProfile> save(UserProfile profile) {
        return repository.save(
            new UserProfileEntity(
                profile.getUserId(), 
                profile.getEmail(), 
                profile.getDisplayName()
            )
        ).map(e -> profile);
    }
}
