package com.taskflow.user.application.service;


import org.springframework.stereotype.Service;

import com.taskflow.user.application.port.in.GetUserProfileUseCase;
import com.taskflow.user.application.port.out.UserProfileRepositoryPort;
import com.taskflow.user.domain.model.UserProfile;
import com.taskflow.user.shared.security.JwtUserContext;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetUserProfileService implements GetUserProfileUseCase {

    private final UserProfileRepositoryPort repository;
    private final JwtUserContext userContext;

    @Override
    public Mono<UserProfile> getCurrentUser() {
        return repository.findById(userContext.userId())
            .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }
}
