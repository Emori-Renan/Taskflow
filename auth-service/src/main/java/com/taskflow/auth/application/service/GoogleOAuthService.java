package com.taskflow.auth.application.service;

import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.dto.GoogleUserInfoDTO;
import com.taskflow.auth.application.port.in.GoogleOAuthUseCase;
import com.taskflow.auth.application.port.out.EventPublisherPort;
import com.taskflow.auth.application.port.out.GoogleOAuthPort;
import com.taskflow.auth.application.port.out.RefreshTokenStoragePort;
import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.event.UserCreatedEvent;
import com.taskflow.auth.domain.model.User;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GoogleOAuthService implements GoogleOAuthUseCase {

    private final GoogleOAuthPort googleOAuthPort;
    private final UserRepositoryPort userRepository;
    private final TokenProviderPort tokenProvider;
    private final RefreshTokenStoragePort refreshTokenStorage;
    private final EventPublisherPort eventPublisher;

    @org.springframework.beans.factory.annotation.Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public GoogleOAuthService(GoogleOAuthPort googleOAuthPort,
                              UserRepositoryPort userRepository,
                              TokenProviderPort tokenProvider,
                              RefreshTokenStoragePort refreshTokenStorage,
                              EventPublisherPort eventPublisher) {
        this.googleOAuthPort = googleOAuthPort;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.refreshTokenStorage = refreshTokenStorage;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String getAuthorizationUrl() {
        return googleOAuthPort.getAuthorizationUrl();
    }

    @Override
    @Transactional
    public AuthResponseDTO handleCallback(String code) {
        GoogleUserInfoDTO googleUser = googleOAuthPort.exchangeCodeForUserInfo(code);

        Optional<User> existingUser = userRepository.findByEmail(googleUser.email());

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (user.oauthProvider() == null) {
                // Account linking: existing email/password user logs in via Google
                User linked = new User(user.id(), user.email(), user.password(), user.role(), "GOOGLE");
                user = userRepository.update(linked);
            }
        } else {
            // New OAuth user
            User newUser = User.ofOAuth(googleUser.email(), "USER", "GOOGLE");
            user = userRepository.save(newUser);
            eventPublisher.publish(new UserCreatedEvent(user.id(), user.email()));
        }

        String accessToken = tokenProvider.generateToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);
        refreshTokenStorage.store(user.email(), refreshToken, refreshExpiration);

        return new AuthResponseDTO(accessToken, refreshToken, user.email(), user.role());
    }
}
