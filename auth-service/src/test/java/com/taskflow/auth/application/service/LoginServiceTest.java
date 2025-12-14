package com.taskflow.auth.application.service;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.exception.InvalidCredentialsException;
import com.taskflow.auth.domain.exception.UserNotFoundException;
import com.taskflow.auth.domain.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

    @InjectMocks
    private LoginService loginService;

    private AuthRequestDTO validRequest;
    private User testUser;

    private static final String RAW_PASSWORD = "CorrectPassword123";
    private static final String HASHED_PASSWORD = "$2a$10$hashedpasswordfromdb";
    private static final String MOCK_TOKEN = "mocked.jwt.token.login";

    @BeforeEach
    void setUp() {
        validRequest = new AuthRequestDTO("user@example.com", RAW_PASSWORD);
        testUser = new User(
                UUID.randomUUID(),
                "user@example.com",
                HASHED_PASSWORD,
                "ROLE_ADMIN"
        );
    }

    @Test
    void login_shouldReturnAuthResponse_whenCredentialsAreValid() {
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Mono.just(testUser));

        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD))
                .thenReturn(true);

        when(tokenProvider.generateToken(testUser))
                .thenReturn(MOCK_TOKEN);

        StepVerifier.create(loginService.login(validRequest))
                .assertNext(response -> {
                    assertEquals(MOCK_TOKEN, response.token(), "Token must match mock token.");
                    assertEquals("user@example.com", response.email(), "Email must match.");
                    assertEquals("ROLE_ADMIN", response.role(), "Role must match.");
                })
                .verifyComplete();

        verify(userRepository).findByEmail("user@example.com");
        verify(passwordEncoder).matches(RAW_PASSWORD, HASHED_PASSWORD);
        verify(tokenProvider).generateToken(testUser);
    }

    @Test
    void login_shouldError_whenUserNotFound() {
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Mono.empty());

        StepVerifier.create(loginService.login(validRequest))
                .expectError(UserNotFoundException.class)
                .verify();

        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    void login_shouldError_whenPasswordIsInvalid() {
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Mono.just(testUser));

        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD))
                .thenReturn(false);

        StepVerifier.create(loginService.login(validRequest))
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(tokenProvider, never()).generateToken(any());
    }
}
