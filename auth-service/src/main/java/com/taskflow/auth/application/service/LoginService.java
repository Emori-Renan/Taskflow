package com.taskflow.auth.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.port.in.LoginUseCase;
import com.taskflow.auth.application.port.out.RefreshTokenStoragePort;
import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.exception.InvalidCredentialsException;
import com.taskflow.auth.domain.exception.UserNotFoundException;
import com.taskflow.auth.domain.model.User;

@Service
public class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenProviderPort tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenStoragePort refreshTokenStorage;

    @org.springframework.beans.factory.annotation.Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public LoginService(UserRepositoryPort userRepository,
                        TokenProviderPort tokenProvider,
                        PasswordEncoder passwordEncoder,
                        RefreshTokenStoragePort refreshTokenStorage) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenStorage = refreshTokenStorage;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(AuthRequestDTO request) {
        // 1. Find user by email
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 2. Validate password
        if (!passwordEncoder.matches(request.password(), user.password())) {
            throw new InvalidCredentialsException();
        }

        // 3. Generate tokens and return response
        String accessToken = tokenProvider.generateToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);
        refreshTokenStorage.store(user.email(), refreshToken, refreshExpiration);

        return new AuthResponseDTO(accessToken, refreshToken, user.email(), user.role());
    }
}
