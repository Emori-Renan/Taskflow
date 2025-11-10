package com.taskflow.auth.infrastructure.adapter.in.web;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.port.in.AuthUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth", description = "Auth management APIs")
@RestController
@RequestMapping("/api/auth") 
public record AuthController(AuthUseCase authUseCase) {

    @Operation(summary = "User login", description = "Authenticate user and return auth token")
    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody AuthRequestDTO request) {
        return authUseCase.login(request);
    }

    @Operation(summary = "User registration", description = "Register a new user")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED) 
    public AuthResponseDTO register(@RequestBody AuthRequestDTO request) {
        return authUseCase.register(request);
    }
}