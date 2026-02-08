package com.taskflow.auth.application.service;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.port.out.RefreshTokenStoragePort;
import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.exception.InvalidCredentialsException;
import com.taskflow.auth.domain.exception.UserNotFoundException;
import com.taskflow.auth.domain.model.User;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenStoragePort refreshTokenStorage;

    private MeterRegistry meterRegistry;
    private LoginService loginService;

    private AuthRequestDTO validRequest;
    private User testUser;

    private static final String RAW_PASSWORD = "CorrectPassword123";
    private static final String HASHED_PASSWORD = "$2a$10$hashedpasswordfromdb";
    private static final String MOCK_TOKEN = "mocked.jwt.token.login";
    private static final String MOCK_REFRESH_TOKEN = "mocked.jwt.refresh.token";

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        loginService = new LoginService(userRepository, tokenProvider, passwordEncoder,
                refreshTokenStorage, meterRegistry);
        validRequest = new AuthRequestDTO("user@example.com", RAW_PASSWORD);
        testUser = new User(
                UUID.randomUUID(),
                "user@example.com",
                HASHED_PASSWORD,
                "ROLE_ADMIN"
        );
        ReflectionTestUtils.setField(loginService, "refreshExpiration", 604800000L);
    }

    @Test
    void login_shouldReturnAuthResponse_whenCredentialsAreValid() {
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD))
                .thenReturn(true);

        when(tokenProvider.generateToken(testUser))
                .thenReturn(MOCK_TOKEN);

        when(tokenProvider.generateRefreshToken(testUser))
                .thenReturn(MOCK_REFRESH_TOKEN);

        var response = loginService.login(validRequest);

        assertEquals(MOCK_TOKEN, response.accessToken(), "Access token must match mock token.");
        assertEquals(MOCK_REFRESH_TOKEN, response.refreshToken(), "Refresh token must match mock token.");
        assertEquals("user@example.com", response.email(), "Email must match.");
        assertEquals("ROLE_ADMIN", response.role(), "Role must match.");

        verify(userRepository).findByEmail("user@example.com");
        verify(passwordEncoder).matches(RAW_PASSWORD, HASHED_PASSWORD);
        verify(tokenProvider).generateToken(testUser);
        verify(tokenProvider).generateRefreshToken(testUser);
        verify(refreshTokenStorage).store(eq("user@example.com"), eq(MOCK_REFRESH_TOKEN), eq(604800000L));
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> loginService.login(validRequest));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    void login_shouldThrowException_whenPasswordIsInvalid() {
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> loginService.login(validRequest));

        verify(tokenProvider, never()).generateToken(any());
    }
}
