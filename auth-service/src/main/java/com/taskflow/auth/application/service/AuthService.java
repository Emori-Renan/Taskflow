package com.taskflow.auth.application.service;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.port.in.AuthUseCase;
import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.application.port.out.UserRepositoryPort;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenProviderPort tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepositoryPort userRepository,
                       TokenProviderPort tokenProvider,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        var user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.password())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        var token = tokenProvider.generateToken(user);
        return new AuthResponseDTO(token, user.username(), user.role());
    }
}
