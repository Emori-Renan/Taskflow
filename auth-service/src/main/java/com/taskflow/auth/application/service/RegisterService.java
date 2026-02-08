package com.taskflow.auth.application.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.port.in.RegisterUseCase;
import com.taskflow.auth.application.port.out.RefreshTokenStoragePort;
import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.exception.InvalidInputException;
import com.taskflow.auth.domain.exception.UserAlreadyExistsException;
import com.taskflow.auth.domain.model.User;

@Service
public class RegisterService implements RegisterUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenProviderPort tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenStoragePort refreshTokenStorage;
    private final Counter registrationSuccessCounter;
    private final Counter registrationFailureCounter;

    @org.springframework.beans.factory.annotation.Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public RegisterService(UserRepositoryPort userRepository,
            TokenProviderPort tokenProvider,
            PasswordEncoder passwordEncoder,
            RefreshTokenStoragePort refreshTokenStorage,
            MeterRegistry meterRegistry) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenStorage = refreshTokenStorage;
        this.registrationSuccessCounter = Counter.builder("auth.registration")
                .tag("outcome", "success")
                .register(meterRegistry);
        this.registrationFailureCounter = Counter.builder("auth.registration")
                .tag("outcome", "failure")
                .register(meterRegistry);
    }

    @Override
    @Transactional
    public AuthResponseDTO register(AuthRequestDTO request) {
        try {
            // 1. Validate input
            if (request.email() == null || request.email().isBlank()) {
                throw new InvalidInputException("Email cannot be empty.");
            }

            if (request.password() == null || request.password().isBlank()) {
                throw new InvalidInputException("Password cannot be empty.");
            }

            // 2. Check if user already exists
            if (userRepository.findByEmail(request.email()).isPresent()) {
                throw new UserAlreadyExistsException(request.email());
            }

            // 3. Save new user (password will be hashed in adapter)
            User newUser = new User(
                request.email(),
                request.password(),
                "USER"
            );
            User savedUser = userRepository.save(newUser);

            // 4. Generate tokens and return response
            String accessToken = tokenProvider.generateToken(savedUser);
            String refreshToken = tokenProvider.generateRefreshToken(savedUser);
            refreshTokenStorage.store(savedUser.email(), refreshToken, refreshExpiration);

            registrationSuccessCounter.increment();
            return new AuthResponseDTO(accessToken, refreshToken, savedUser.email(), savedUser.role());
        } catch (Exception e) {
            registrationFailureCounter.increment();
            throw e;
        }
    }
}
