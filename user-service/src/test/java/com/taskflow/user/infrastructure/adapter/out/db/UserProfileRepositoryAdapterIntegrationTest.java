package com.taskflow.user.infrastructure.adapter.out.db;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.taskflow.user.domain.model.UserProfile;
import com.taskflow.user.infrastructure.adapter.out.db.entity.UserProfileEntity;
import com.taskflow.user.infrastructure.adapter.out.db.repository.UserProfileRepositoryAdapter;

@ActiveProfiles("test")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserProfileRepositoryAdapter.class)
class UserProfileRepositoryAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserProfileRepositoryAdapter repositoryAdapter;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_shouldPersistUserProfile() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile(userId, "test@example.com");

        UserProfile saved = repositoryAdapter.save(profile);

        assertNotNull(saved);
        assertEquals(userId, saved.getUserId());
        assertEquals("test@example.com", saved.getEmail());

        UserProfileEntity entity = entityManager.find(UserProfileEntity.class, userId);
        assertNotNull(entity);
        assertEquals("test@example.com", entity.getEmail());
    }

    @Test
    void findById_shouldReturnProfileWhenExists() {
        UUID userId = UUID.randomUUID();
        UserProfileEntity entity = new UserProfileEntity(userId, "found@example.com", "Display", "http://avatar.url");
        entityManager.persist(entity);
        entityManager.flush();

        Optional<UserProfile> result = repositoryAdapter.findById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUserId());
        assertEquals("found@example.com", result.get().getEmail());
        assertEquals("Display", result.get().getDisplayName());
        assertEquals("http://avatar.url", result.get().getAvatarUrl());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        Optional<UserProfile> result = repositoryAdapter.findById(UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    void save_shouldUpdateExistingProfile() {
        UUID userId = UUID.randomUUID();
        UserProfileEntity entity = new UserProfileEntity(userId, "update@example.com", null, null);
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        UserProfile profile = new UserProfile(userId, "update@example.com");
        profile.updateProfile("Updated Name", "http://new-avatar.url");

        UserProfile updated = repositoryAdapter.save(profile);

        assertEquals("Updated Name", updated.getDisplayName());
        assertEquals("http://new-avatar.url", updated.getAvatarUrl());
    }
}
