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
import static org.junit.jupiter.api.Assertions.*; // <-- NEW: Static import for Assertions

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
        validRequest = new AuthRequestDTO("existinguser", RAW_PASSWORD);
        testUser = new User(
                UUID.randomUUID(),
                "existinguser",
                HASHED_PASSWORD,
                "ROLE_ADMIN"
        );
    }

    @Test
    void login_shouldReturnAuthResponse_whenCredentialsAreValid() {
        // Arrange
        when(userRepository.findByUsername("existinguser"))
                .thenReturn(Mono.just(testUser));

        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD))
                .thenReturn(true);
        
        // NOTE: In a reactive LoginService, tokenProvider.generateToken must return Mono<String>
        // If it returns String, this test is slightly incorrect for a purely reactive flow, 
        // but we'll mock it as you provided for now.
        when(tokenProvider.generateToken(testUser))
                .thenReturn(MOCK_TOKEN); 

        // Act & Assert
        StepVerifier.create(loginService.login(validRequest))
                .assertNext(response -> {
                    // FIX: Replaced 'assert' keyword with assertEquals()
                    assertEquals(MOCK_TOKEN, response.token(), "Token must match mock token.");
                    assertEquals("existinguser", response.username(), "Username must match.");
                    assertEquals("ROLE_ADMIN", response.role(), "Role must match.");
                })
                .verifyComplete();

        verify(userRepository).findByUsername("existinguser");
        verify(passwordEncoder).matches(RAW_PASSWORD, HASHED_PASSWORD);
        verify(tokenProvider).generateToken(testUser);
    }

    @Test
    void login_shouldError_whenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("existinguser"))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(loginService.login(validRequest))
                .expectError(UserNotFoundException.class)
                .verify();

        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    void login_shouldError_whenPasswordIsInvalid() {
        // Arrange
        when(userRepository.findByUsername("existinguser"))
                .thenReturn(Mono.just(testUser));

        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD))
                .thenReturn(false);

        // Act & Assert
        StepVerifier.create(loginService.login(validRequest))
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(tokenProvider, never()).generateToken(any());
    }
}