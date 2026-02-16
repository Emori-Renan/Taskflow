package com.taskflow.user.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskflow.user.application.port.out.UserProfileRepositoryPort;
import com.taskflow.user.domain.model.UserProfile;

@ExtendWith(MockitoExtension.class)
class CreateUserProfileServiceTest {

    @Mock
    private UserProfileRepositoryPort repository;

    @InjectMocks
    private CreateUserProfileService service;

    @Test
    void create_shouldSaveWithCorrectUuidAndEmail() {
        UUID userId = UUID.randomUUID();
        String email = "newuser@example.com";

        when(repository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfile result = service.create(userId, email);

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(repository).save(captor.capture());

        UserProfile saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(email, saved.getEmail());
        assertEquals(userId, result.getUserId());
        assertEquals(email, result.getEmail());
    }
}
