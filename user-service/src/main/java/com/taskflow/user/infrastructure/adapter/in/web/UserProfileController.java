package com.taskflow.user.infrastructure.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.user.application.dto.UserProfileResponse;
import com.taskflow.user.application.port.in.GetUserProfileUseCase;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final GetUserProfileUseCase useCase;

    @GetMapping("/me")
    public Mono<UserProfileResponse> me() {
        return useCase.getCurrentUser()
            .map(UserProfileResponse::from);
    }
}
