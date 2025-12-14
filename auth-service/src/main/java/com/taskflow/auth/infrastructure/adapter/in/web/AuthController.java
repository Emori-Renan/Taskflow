package com.taskflow.auth.infrastructure.adapter.in.web;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.port.in.LoginUseCase;
import com.taskflow.auth.application.port.in.RegisterUseCase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@Tag(name = "Auth", description = "Auth management APIs")
@RestController
@RequestMapping("/api/auth")
public record AuthController(
        LoginUseCase loginUseCase,
        RegisterUseCase registerUseCase
) {

    @Operation(summary = "User login", description = "Authenticate user and return auth token")
    @PostMapping("/login")
    public Mono<AuthResponseDTO> login(
            @RequestBody Mono<AuthRequestDTO> request
    ) {
        return request.flatMap(loginUseCase::login);
    }

    @Operation(summary = "User registration", description = "Register a new user")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AuthResponseDTO> register(
            @RequestBody Mono<AuthRequestDTO> request
    ) {
        return request.flatMap(registerUseCase::register);
    }
}
