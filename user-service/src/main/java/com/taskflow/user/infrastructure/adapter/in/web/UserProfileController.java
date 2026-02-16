package com.taskflow.user.infrastructure.adapter.in.web;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.user.application.dto.UpdateUserProfileRequest;
import com.taskflow.user.application.dto.UserProfileResponse;
import com.taskflow.user.application.port.in.GetUserProfileUseCase;
import com.taskflow.user.application.port.in.UpdateUserProfileUseCase;
import com.taskflow.user.shared.security.JwtUserContext;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserProfileController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final JwtUserContext userContext;

    @GetMapping("/me")
    public UserProfileResponse me() {
        return UserProfileResponse.from(getUserProfileUseCase.getCurrentUser());
    }

    @PutMapping("/me")
    public UserProfileResponse updateMe(@RequestBody UpdateUserProfileRequest request) {
        UUID userId = userContext.userId();
        return UserProfileResponse.from(
            updateUserProfileUseCase.update(userId, request.displayName(), request.avatarUrl())
        );
    }

    @GetMapping("/{id}")
    public UserProfileResponse getById(@PathVariable UUID id) {
        return UserProfileResponse.from(getUserProfileUseCase.getById(id));
    }
}
