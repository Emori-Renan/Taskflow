package com.taskflow.auth.application.service;

import com.taskflow.auth.application.dto.RefreshRequestDTO;
import com.taskflow.auth.application.port.out.RefreshTokenStoragePort;
import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.exception.InvalidTokenException;
import com.taskflow.auth.domain.exception.UserNotFoundException;
import com.taskflow.auth.domain.model.User;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private RefreshTokenStoragePort refreshTokenStorage;

    @Mock
    private UserRepositoryPort userRepository;

    private MeterRegistry meterRegistry;
    private RefreshTokenService refreshTokenService;

    private User testUser;
    private static final String OLD_REFRESH_TOKEN = "old.refresh.token";
    private static final String NEW_ACCESS_TOKEN = "new.access.token";
    private static final String NEW_REFRESH_TOKEN = "new.refresh.token";
    private static final String TEST_EMAIL = "user@example.com";

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        refreshTokenService = new RefreshTokenService(tokenProvider, refreshTokenStorage,
                userRepository, meterRegistry);
        testUser = new User(UUID.randomUUID(), TEST_EMAIL, "hashedpassword", "USER");
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpiration", 604800000L);
    }

    @Test
    void refresh_shouldReturnNewTokens_whenRefreshTokenIsValid() {
        RefreshRequestDTO request = new RefreshRequestDTO(OLD_REFRESH_TOKEN);

        when(tokenProvider.isRefreshTokenValid(OLD_REFRESH_TOKEN)).thenReturn(true);
        when(tokenProvider.extractEmailFromRefreshToken(OLD_REFRESH_TOKEN)).thenReturn(TEST_EMAIL);
        when(refreshTokenStorage.get(TEST_EMAIL)).thenReturn(OLD_REFRESH_TOKEN);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateToken(testUser)).thenReturn(NEW_ACCESS_TOKEN);
        when(tokenProvider.generateRefreshToken(testUser)).thenReturn(NEW_REFRESH_TOKEN);

        var response = refreshTokenService.refresh(request);

        assertEquals(NEW_ACCESS_TOKEN, response.accessToken());
        assertEquals(NEW_REFRESH_TOKEN, response.refreshToken());
        assertEquals(TEST_EMAIL, response.email());
        assertEquals("USER", response.role());

        verify(refreshTokenStorage).store(eq(TEST_EMAIL), eq(NEW_REFRESH_TOKEN), eq(604800000L));
    }

    @Test
    void refresh_shouldThrowInvalidTokenException_whenTokenIsInvalid() {
        RefreshRequestDTO request = new RefreshRequestDTO("invalid.token");

        when(tokenProvider.isRefreshTokenValid("invalid.token")).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> refreshTokenService.refresh(request));

        verify(refreshTokenStorage, never()).get(any());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void refresh_shouldThrowInvalidTokenException_whenTokenNotInRedis() {
        RefreshRequestDTO request = new RefreshRequestDTO(OLD_REFRESH_TOKEN);

        when(tokenProvider.isRefreshTokenValid(OLD_REFRESH_TOKEN)).thenReturn(true);
        when(tokenProvider.extractEmailFromRefreshToken(OLD_REFRESH_TOKEN)).thenReturn(TEST_EMAIL);
        when(refreshTokenStorage.get(TEST_EMAIL)).thenReturn(null);

        assertThrows(InvalidTokenException.class, () -> refreshTokenService.refresh(request));

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void refresh_shouldThrowInvalidTokenException_whenTokenMismatchInRedis() {
        RefreshRequestDTO request = new RefreshRequestDTO(OLD_REFRESH_TOKEN);

        when(tokenProvider.isRefreshTokenValid(OLD_REFRESH_TOKEN)).thenReturn(true);
        when(tokenProvider.extractEmailFromRefreshToken(OLD_REFRESH_TOKEN)).thenReturn(TEST_EMAIL);
        when(refreshTokenStorage.get(TEST_EMAIL)).thenReturn("different.token");

        assertThrows(InvalidTokenException.class, () -> refreshTokenService.refresh(request));

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void refresh_shouldThrowUserNotFoundException_whenUserNotFound() {
        RefreshRequestDTO request = new RefreshRequestDTO(OLD_REFRESH_TOKEN);

        when(tokenProvider.isRefreshTokenValid(OLD_REFRESH_TOKEN)).thenReturn(true);
        when(tokenProvider.extractEmailFromRefreshToken(OLD_REFRESH_TOKEN)).thenReturn(TEST_EMAIL);
        when(refreshTokenStorage.get(TEST_EMAIL)).thenReturn(OLD_REFRESH_TOKEN);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> refreshTokenService.refresh(request));
    }
}
