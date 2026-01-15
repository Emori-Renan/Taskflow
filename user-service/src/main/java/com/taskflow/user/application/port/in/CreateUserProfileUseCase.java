package com.taskflow.user.application.port.in;

import java.util.UUID;

import com.taskflow.user.domain.model.UserProfile;

import reactor.core.publisher.Mono;

public interface CreateUserProfileUseCase {
        Mono<UserProfile> create(UUID userId, String email);
}
