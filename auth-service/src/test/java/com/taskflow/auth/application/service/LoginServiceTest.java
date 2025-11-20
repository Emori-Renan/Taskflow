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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    // Mocks for the ports and Spring security utility
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private TokenProviderPort tokenProvider;
    @Mock
    private PasswordEncoder passwordEncoder;

    // Inject the mocks into the service being tested
    @InjectMocks
    private LoginService loginService;

    private AuthRequestDTO validRequest;
    private User testUser;
    private final String RAW_PASSWORD = "CorrectPassword123";
    private final String HASHED_PASSWORD = "$2a$10$hashedpasswordfromdb";
    private final String MOCK_TOKEN = "mocked.jwt.token.login";

    @BeforeEach
    void setUp() {
        validRequest = new AuthRequestDTO("existinguser", RAW_PASSWORD);
        testUser = new User(UUID.randomUUID(), "existinguser", HASHED_PASSWORD, "ROLE_ADMIN");
    }

    // --- Successful Flow Test ---
    
    @Test
    void login_shouldReturnAuthResponse_whenCredentialsAreValid() {
        // Arrange
        // 1. Mock: User is found in the database
        when(userRepository.findByUsername(validRequest.username())).thenReturn(Optional.of(testUser));
        // 2. Mock: Password verification succeeds
        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        // 3. Mock: Token is generated
        when(tokenProvider.generateToken(testUser)).thenReturn(MOCK_TOKEN);

        // Act
        var response = loginService.login(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(MOCK_TOKEN, response.token());
        assertEquals("existinguser", response.username());
        assertEquals("ROLE_ADMIN", response.role());
        
        // Verify all required steps were executed once
        verify(userRepository, times(1)).findByUsername(validRequest.username());
        verify(passwordEncoder, times(1)).matches(RAW_PASSWORD, HASHED_PASSWORD);
        verify(tokenProvider, times(1)).generateToken(testUser);
    }

    // --- Failure/Exception Tests ---
    
    @Test
    void login_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByUsername(validRequest.username())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> loginService.login(validRequest));
        
        // Verify password check and token generation were skipped
        verify(passwordEncoder, never()).matches(anyString(), anyString()); 
        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    void login_shouldThrowInvalidCredentialsException_whenPasswordIsInvalid() {
        // Arrange
        when(userRepository.findByUsername(validRequest.username())).thenReturn(Optional.of(testUser));
        // Mock: Password verification fails
        when(passwordEncoder.matches(validRequest.password(), HASHED_PASSWORD)).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> loginService.login(validRequest));
        
        // Verify token generation was skipped
        verify(tokenProvider, never()).generateToken(any());
    }
}