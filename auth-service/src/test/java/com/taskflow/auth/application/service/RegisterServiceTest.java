package com.taskflow.auth.application.service;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.port.out.RefreshTokenStoragePort;
import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.exception.InvalidInputException;
import com.taskflow.auth.domain.exception.UserAlreadyExistsException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

        @Mock
        private UserRepositoryPort userRepository;

        @Mock
        private TokenProviderPort tokenProvider;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private RefreshTokenStoragePort refreshTokenStorage;

        private MeterRegistry meterRegistry;
        private RegisterService registerService;

        private AuthRequestDTO validRequest;

        private static final String RAW_PASSWORD = "TestPassword123";
        private static final String MOCK_TOKEN = "mocked.jwt.token";
        private static final String MOCK_REFRESH_TOKEN = "mocked.jwt.refresh.token";

        @BeforeEach
        void setUp() {
                meterRegistry = new SimpleMeterRegistry();
                registerService = new RegisterService(userRepository, tokenProvider, passwordEncoder,
                        refreshTokenStorage, meterRegistry);
                validRequest = new AuthRequestDTO("newuser@example.com", RAW_PASSWORD);
                ReflectionTestUtils.setField(registerService, "refreshExpiration", 604800000L);
        }

        @Test
        void register_shouldCreateUserAndReturnAuthResponse_whenUserDoesNotExist() {
                User savedUser = new User(
                                UUID.randomUUID(),
                                "newuser@example.com",
                                "hashedPassword",
                                "USER");

                when(userRepository.findByEmail("newuser@example.com"))
                                .thenReturn(Optional.empty());

                when(userRepository.save(any(User.class)))
                                .thenReturn(savedUser);

                when(tokenProvider.generateToken(savedUser))
                                .thenReturn(MOCK_TOKEN);

                when(tokenProvider.generateRefreshToken(savedUser))
                                .thenReturn(MOCK_REFRESH_TOKEN);

                var response = registerService.register(validRequest);

                assertEquals(MOCK_TOKEN, response.accessToken());
                assertEquals(MOCK_REFRESH_TOKEN, response.refreshToken());
                assertEquals("newuser@example.com", response.email());
                assertEquals("USER", response.role());

                verify(userRepository).save(any(User.class));
                verify(tokenProvider).generateToken(savedUser);
                verify(tokenProvider).generateRefreshToken(savedUser);
                verify(refreshTokenStorage).store(eq("newuser@example.com"), eq(MOCK_REFRESH_TOKEN), eq(604800000L));
        }

        @Test
        void register_shouldThrowException_whenUserAlreadyExists() {
                User existingUser = new User(
                                UUID.randomUUID(),
                                "newuser@example.com",
                                "hashed",
                                "USER");

                when(userRepository.findByEmail("newuser@example.com"))
                                .thenReturn(Optional.of(existingUser));

                assertThrows(UserAlreadyExistsException.class,
                                () -> registerService.register(validRequest));

                verify(userRepository, never()).save(any());
                verify(tokenProvider, never()).generateToken(any());
        }

        @Test
        void register_shouldThrowException_whenEmailIsBlank() {
                AuthRequestDTO request = new AuthRequestDTO(" ", RAW_PASSWORD);

                assertThrows(InvalidInputException.class,
                                () -> registerService.register(request));

                verifyNoInteractions(userRepository);
        }

        @Test
        void register_shouldThrowException_whenPasswordIsNull() {
                AuthRequestDTO request = new AuthRequestDTO("validuser@example.com", null);

                assertThrows(InvalidInputException.class,
                                () -> registerService.register(request));

                verifyNoInteractions(userRepository);
        }
}
