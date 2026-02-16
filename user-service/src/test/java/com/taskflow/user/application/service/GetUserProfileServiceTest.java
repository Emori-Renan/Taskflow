package com.taskflow.user.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskflow.user.application.port.out.UserProfileRepositoryPort;
import com.taskflow.user.domain.exception.UserNotFoundException;
import com.taskflow.user.domain.model.UserProfile;
import com.taskflow.user.shared.security.JwtUserContext;

@ExtendWith(MockitoExtension.class)
class GetUserProfileServiceTest {

    @Mock
    private UserProfileRepositoryPort repository;

    @Mock
    private JwtUserContext userContext;

    @InjectMocks
    private GetUserProfileService service;

    @Test
    void getCurrentUser_shouldReturnProfile() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile(userId, "test@example.com");

        when(userContext.userId()).thenReturn(userId);
        when(repository.findById(userId)).thenReturn(Optional.of(profile));

        UserProfile result = service.getCurrentUser();

        assertEquals(userId, result.getUserId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getCurrentUser_shouldThrowWhenNotFound() {
        UUID userId = UUID.randomUUID();

        when(userContext.userId()).thenReturn(userId);
        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.getCurrentUser());
    }

    @Test
    void getById_shouldReturnProfile() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile(userId, "test@example.com");

        when(repository.findById(userId)).thenReturn(Optional.of(profile));

        UserProfile result = service.getById(userId);

        assertEquals(userId, result.getUserId());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        UUID userId = UUID.randomUUID();

        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.getById(userId));
    }
}
