package com.taskflow.auth.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.dto.RefreshRequestDTO;
import com.taskflow.auth.application.port.in.RefreshTokenUseCase;
import com.taskflow.auth.application.port.out.RefreshTokenStoragePort;
import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.exception.InvalidTokenException;
import com.taskflow.auth.domain.exception.UserNotFoundException;
import com.taskflow.auth.domain.model.User;

@Service
public class RefreshTokenService implements RefreshTokenUseCase {

    private final TokenProviderPort tokenProvider;
    private final RefreshTokenStoragePort refreshTokenStorage;
    private final UserRepositoryPort userRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public RefreshTokenService(TokenProviderPort tokenProvider,
                               RefreshTokenStoragePort refreshTokenStorage,
                               UserRepositoryPort userRepository) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenStorage = refreshTokenStorage;
        this.userRepository = userRepository;
    }

    @Override
    public AuthResponseDTO refresh(RefreshRequestDTO request) {
        // 1. Validate refresh token JWT
        if (!tokenProvider.isRefreshTokenValid(request.refreshToken())) {
            throw new InvalidTokenException();
        }

        // 2. Extract email from token
        String email = tokenProvider.extractEmailFromRefreshToken(request.refreshToken());

        // 3. Check token exists in Redis
        String storedToken = refreshTokenStorage.get(email);
        if (storedToken == null || !storedToken.equals(request.refreshToken())) {
            throw new InvalidTokenException();
        }

        // 4. Load user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 5. Generate new tokens
        String accessToken = tokenProvider.generateToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        // 6. Store new refresh token in Redis
        refreshTokenStorage.store(email, refreshToken, refreshExpiration);

        return new AuthResponseDTO(accessToken, refreshToken, user.email(), user.role());
    }
}
