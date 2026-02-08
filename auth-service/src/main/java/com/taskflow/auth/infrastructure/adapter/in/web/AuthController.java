package com.taskflow.auth.infrastructure.adapter.in.web;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.dto.RefreshRequestDTO;
import com.taskflow.auth.application.port.in.LoginUseCase;
import com.taskflow.auth.application.port.in.RefreshTokenUseCase;
import com.taskflow.auth.application.port.in.RegisterUseCase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Auth", description = "Auth management APIs")
@RestController
@RequestMapping("/api/auth")
public record AuthController(
        LoginUseCase loginUseCase,
        RegisterUseCase registerUseCase,
        RefreshTokenUseCase refreshTokenUseCase
) {

    @Operation(summary = "User login", description = "Authenticate user and return auth token")
    @PostMapping("/login")
    public AuthResponseDTO login(
            @Valid @RequestBody AuthRequestDTO request
    ) {
        return loginUseCase.login(request);
    }

    @Operation(summary = "User registration", description = "Register a new user")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO register(
            @Valid @RequestBody AuthRequestDTO request
    ) {
        return registerUseCase.register(request);
    }

    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    @PostMapping("/refresh")
    public AuthResponseDTO refresh(
            @Valid @RequestBody RefreshRequestDTO request
    ) {
        return refreshTokenUseCase.refresh(request);
    }
}
