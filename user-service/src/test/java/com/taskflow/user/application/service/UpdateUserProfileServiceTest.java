package com.taskflow.user.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

@ExtendWith(MockitoExtension.class)
class UpdateUserProfileServiceTest {

    @Mock
    private UserProfileRepositoryPort repository;

    @InjectMocks
    private UpdateUserProfileService service;

    @Test
    void update_shouldLoadUpdateAndSave() {
        UUID userId = UUID.randomUUID();
        UserProfile existing = new UserProfile(userId, "user@example.com");

        when(repository.findById(userId)).thenReturn(Optional.of(existing));
        when(repository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfile result = service.update(userId, "New Name", "http://avatar.url/img.png");

        assertEquals("New Name", result.getDisplayName());
        assertEquals("http://avatar.url/img.png", result.getAvatarUrl());
        verify(repository).save(any(UserProfile.class));
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        UUID userId = UUID.randomUUID();

        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
            () -> service.update(userId, "Name", "http://avatar.url"));
    }
}
