package com.taskflow.auth.application.service;

import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.dto.GoogleUserInfoDTO;
import com.taskflow.auth.application.port.out.EventPublisherPort;
import com.taskflow.auth.application.port.out.GoogleOAuthPort;
import com.taskflow.auth.application.port.out.RefreshTokenStoragePort;
import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.event.UserCreatedEvent;
import com.taskflow.auth.domain.exception.OAuthException;
import com.taskflow.auth.domain.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthServiceTest {

    @Mock
    private GoogleOAuthPort googleOAuthPort;

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private RefreshTokenStoragePort refreshTokenStorage;

    @Mock
    private EventPublisherPort eventPublisher;

    private GoogleOAuthService googleOAuthService;

    private static final String MOCK_TOKEN = "mocked.jwt.token";
    private static final String MOCK_REFRESH_TOKEN = "mocked.jwt.refresh.token";
    private static final String AUTH_CODE = "google-auth-code";

    @BeforeEach
    void setUp() {
        googleOAuthService = new GoogleOAuthService(
                googleOAuthPort, userRepository, tokenProvider,
                refreshTokenStorage, eventPublisher);
        ReflectionTestUtils.setField(googleOAuthService, "refreshExpiration", 604800000L);
    }

    @Test
    void getAuthorizationUrl_shouldDelegateToPort() {
        String expectedUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=test";
        when(googleOAuthPort.getAuthorizationUrl()).thenReturn(expectedUrl);

        String result = googleOAuthService.getAuthorizationUrl();

        assertEquals(expectedUrl, result);
        verify(googleOAuthPort).getAuthorizationUrl();
    }

    @Test
    void handleCallback_shouldCreateNewUserAndPublishEvent_whenUserDoesNotExist() {
        GoogleUserInfoDTO googleUser = new GoogleUserInfoDTO("new@example.com", "New User", "http://pic.url");
        UUID userId = UUID.randomUUID();
        User savedUser = new User(userId, "new@example.com", null, "USER", "GOOGLE");

        when(googleOAuthPort.exchangeCodeForUserInfo(AUTH_CODE)).thenReturn(googleUser);
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateToken(savedUser)).thenReturn(MOCK_TOKEN);
        when(tokenProvider.generateRefreshToken(savedUser)).thenReturn(MOCK_REFRESH_TOKEN);

        AuthResponseDTO response = googleOAuthService.handleCallback(AUTH_CODE);

        assertEquals(MOCK_TOKEN, response.accessToken());
        assertEquals(MOCK_REFRESH_TOKEN, response.refreshToken());
        assertEquals("new@example.com", response.email());
        assertEquals("USER", response.role());

        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals(userId, eventCaptor.getValue().userId());
        assertEquals("new@example.com", eventCaptor.getValue().email());

        verify(userRepository).save(any(User.class));
        verify(userRepository, never()).update(any());
    }

    @Test
    void handleCallback_shouldLoginExistingGoogleUser_withoutPublishingEvent() {
        GoogleUserInfoDTO googleUser = new GoogleUserInfoDTO("existing@example.com", "Existing", "http://pic.url");
        User existingUser = new User(UUID.randomUUID(), "existing@example.com", null, "USER", "GOOGLE");

        when(googleOAuthPort.exchangeCodeForUserInfo(AUTH_CODE)).thenReturn(googleUser);
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));
        when(tokenProvider.generateToken(existingUser)).thenReturn(MOCK_TOKEN);
        when(tokenProvider.generateRefreshToken(existingUser)).thenReturn(MOCK_REFRESH_TOKEN);

        AuthResponseDTO response = googleOAuthService.handleCallback(AUTH_CODE);

        assertEquals(MOCK_TOKEN, response.accessToken());
        assertEquals("existing@example.com", response.email());

        verify(eventPublisher, never()).publish(any());
        verify(userRepository, never()).save(any());
        verify(userRepository, never()).update(any());
    }

    @Test
    void handleCallback_shouldLinkAccount_whenEmailMatchesExistingPasswordUser() {
        GoogleUserInfoDTO googleUser = new GoogleUserInfoDTO("linked@example.com", "Linked", "http://pic.url");
        UUID userId = UUID.randomUUID();
        User existingUser = new User(userId, "linked@example.com", "hashedPassword", "USER", null);
        User linkedUser = new User(userId, "linked@example.com", "hashedPassword", "USER", "GOOGLE");

        when(googleOAuthPort.exchangeCodeForUserInfo(AUTH_CODE)).thenReturn(googleUser);
        when(userRepository.findByEmail("linked@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.update(any(User.class))).thenReturn(linkedUser);
        when(tokenProvider.generateToken(linkedUser)).thenReturn(MOCK_TOKEN);
        when(tokenProvider.generateRefreshToken(linkedUser)).thenReturn(MOCK_REFRESH_TOKEN);

        AuthResponseDTO response = googleOAuthService.handleCallback(AUTH_CODE);

        assertEquals(MOCK_TOKEN, response.accessToken());

        verify(userRepository).update(any(User.class));
        verify(eventPublisher, never()).publish(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void handleCallback_shouldThrowOAuthException_whenGoogleApiFails() {
        when(googleOAuthPort.exchangeCodeForUserInfo(AUTH_CODE))
                .thenThrow(new OAuthException("Failed to exchange authorization code with Google: Connection refused"));

        assertThrows(OAuthException.class,
                () -> googleOAuthService.handleCallback(AUTH_CODE));

        verify(userRepository, never()).findByEmail(any());
        verify(eventPublisher, never()).publish(any());
    }
}
