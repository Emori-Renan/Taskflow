package com.taskflow.auth.application.service;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.exception.InvalidInputException;
import com.taskflow.auth.domain.exception.UserAlreadyExistsException;
import com.taskflow.auth.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    // Mocks for the ports (dependencies)
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private TokenProviderPort tokenProvider;

    // Inject the mocks into the service being tested
    @InjectMocks
    private RegisterService registerService;

    private AuthRequestDTO validRequest;
    private final String RAW_PASSWORD = "TestPassword123";
    private final String MOCK_TOKEN = "mocked.jwt.token";

    @BeforeEach
    void setUp() {
        validRequest = new AuthRequestDTO("newuser", RAW_PASSWORD);
    }

    // --- Successful Flow Test ---
    
    @Test
    void register_shouldCreateUserAndReturnAuthResponse_whenUserDoesNotExist() {
        // Arrange
        User savedUserWithId = new User(UUID.randomUUID(), "newuser", RAW_PASSWORD, "USER");
        
        // 1. Mock: User not found (ready to register)
        when(userRepository.findByUsername(validRequest.username())).thenReturn(Optional.empty());
        // 2. Mock: Repository saves user and returns a user object (with generated ID/hashed password)
        when(userRepository.save(any(User.class))).thenReturn(savedUserWithId);
        // 3. Mock: Token is generated
        when(tokenProvider.generateToken(savedUserWithId)).thenReturn(MOCK_TOKEN);

        // Act
        var response = registerService.register(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(MOCK_TOKEN, response.token());
        assertEquals("newuser", response.username());
        assertEquals("USER", response.role());
        
        // Verify that the repository was called to save the user
        verify(userRepository, times(1)).save(argThat(user -> 
            user.username().equals("newuser") && user.password().equals(RAW_PASSWORD) && user.role().equals("USER")
        ));
        verify(tokenProvider, times(1)).generateToken(savedUserWithId);
    }
    
    // --- Failure/Exception Tests ---

    @Test
    void register_shouldThrowUserAlreadyExistsException_whenUserExists() {
        // Arrange
        User existingUser = new User(UUID.randomUUID(), "newuser", "hashedpass", "USER");
        when(userRepository.findByUsername(validRequest.username())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> registerService.register(validRequest));
        
        // Verify that the save and token generation steps were NOT executed
        verify(userRepository, never()).save(any(User.class));
        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    void register_shouldThrowInvalidInputException_whenUsernameIsEmpty() {
        // Arrange
        AuthRequestDTO emptyUsernameRequest = new AuthRequestDTO(" ", RAW_PASSWORD);

        // Act & Assert
        assertThrows(InvalidInputException.class, () -> registerService.register(emptyUsernameRequest));
        
        // Verify no repository calls were made
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void register_shouldThrowInvalidInputException_whenPasswordIsNull() {
        // Arrange
        AuthRequestDTO nullPasswordRequest = new AuthRequestDTO("validuser", null);

        // Act & Assert
        assertThrows(InvalidInputException.class, () -> registerService.register(nullPasswordRequest));
    }
}