package com.taskflow.user.application.port.out;

import java.util.UUID;

import com.taskflow.user.domain.model.UserProfile;

import reactor.core.publisher.Mono;

public interface UserProfileRepositoryPort {
    Mono<UserProfile> findById(UUID id);
    Mono<UserProfile> save(UserProfile profile);
}
