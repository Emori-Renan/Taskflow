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

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private TokenProviderPort tokenProvider;

    @InjectMocks
    private RegisterService registerService;

    private AuthRequestDTO validRequest;

    private static final String RAW_PASSWORD = "TestPassword123";
    private static final String MOCK_TOKEN = "mocked.jwt.token";

    @BeforeEach
    void setUp() {
        validRequest = new AuthRequestDTO("newuser", RAW_PASSWORD);
    }

    @Test
    void register_shouldCreateUserAndReturnAuthResponse_whenUserDoesNotExist() {
        // Arrange
        User savedUser = new User(
                UUID.randomUUID(),
                "newuser",
                "hashedPassword",
                "USER"
        );

        when(userRepository.findByUsername("newuser"))
                .thenReturn(Mono.empty());

        when(userRepository.save(any(User.class)))
                .thenReturn(Mono.just(savedUser));

        when(tokenProvider.generateToken(savedUser))
                .thenReturn(MOCK_TOKEN);

        // Act & Assert
        StepVerifier.create(registerService.register(validRequest))
                .assertNext(response -> {
                    assert response.token().equals(MOCK_TOKEN);
                    assert response.username().equals("newuser");
                    assert response.role().equals("USER");
                })
                .verifyComplete();

        verify(userRepository).save(any(User.class));
        verify(tokenProvider).generateToken(savedUser);
    }

    @Test
    void register_shouldError_whenUserAlreadyExists() {
        // Arrange
        User existingUser = new User(
                UUID.randomUUID(),
                "newuser",
                "hashed",
                "USER"
        );

        when(userRepository.findByUsername("newuser"))
                .thenReturn(Mono.just(existingUser));

        // Act & Assert
        StepVerifier.create(registerService.register(validRequest))
                .expectError(UserAlreadyExistsException.class)
                .verify();

        verify(userRepository, never()).save(any());
        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    void register_shouldError_whenUsernameIsBlank() {
        AuthRequestDTO request = new AuthRequestDTO(" ", RAW_PASSWORD);

        StepVerifier.create(registerService.register(request))
                .expectError(InvalidInputException.class)
                .verify();

        verifyNoInteractions(userRepository);
    }

    @Test
    void register_shouldError_whenPasswordIsNull() {
        AuthRequestDTO request = new AuthRequestDTO("validuser", null);

        StepVerifier.create(registerService.register(request))
                .expectError(InvalidInputException.class)
                .verify();

        verifyNoInteractions(userRepository);
    }
}
