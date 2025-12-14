package com.taskflow.auth.infrastructure.adapter.out.db;

import com.taskflow.auth.domain.model.User;
import com.taskflow.auth.infrastructure.adapter.out.db.entity.UserEntity;
import com.taskflow.auth.infrastructure.adapter.out.db.repository.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.test.StepVerifier;

// Static import for JUnit 5 assertion methods (Fixes the syntax error)
import static org.junit.jupiter.api.Assertions.*; 

@DataR2dbcTest
@Import({ UserRepositoryAdapter.class, BCryptPasswordEncoder.class })
class UserRepositoryAdapterIntegrationTest {

    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    @Autowired
    private SpringDataUserRepository springDataRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void save_shouldPersistUserAndHashPassword() {
        // Arrange
        User rawUser = new User(
                null,
                "newuser",
                "rawPassword123",
                "ROLE_MEMBER"
        );

        // Act & Assert: Adapter returns domain model
        StepVerifier.create(userRepositoryAdapter.save(rawUser))
                .assertNext(savedUser -> {
                    // Using JUnit 5 Assertions
                    assertNotNull(savedUser.id(), "User ID should not be null after save.");
                    assertEquals("newuser", savedUser.username(), "Username must match.");
                    assertEquals("ROLE_MEMBER", savedUser.role(), "Role must match.");
                })
                .verifyComplete();

        // Assert: Database contains hashed password
        StepVerifier.create(springDataRepository.findByUsername("newuser"))
                .assertNext(entity ->
                        // Using JUnit 5 Assertions
                        assertTrue(
                                passwordEncoder.matches(
                                        "rawPassword123",
                                        entity.getPassword()
                                ),
                                "Password stored in DB must match the raw password after hashing."
                        )
                )
                .verifyComplete();
    }

    @Test
    void findByUsername_shouldReturnDomainModelIfFound() {
        // Arrange
        String rawPassword = "testpass";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        UserEntity entity = new UserEntity(
                null,
                "existinguser",
                hashedPassword,
                "ROLE_ADMIN"
        );

        // Pre-save the entity directly using the Spring Data repository
        StepVerifier.create(springDataRepository.save(entity))
                .expectNextCount(1)
                .verifyComplete();

        // Act & Assert
        StepVerifier.create(userRepositoryAdapter.findByUsername("existinguser"))
                .assertNext(user -> {
                    // Using JUnit 5 Assertions
                    assertNotNull(user.id(), "User ID should not be null.");
                    assertEquals("existinguser", user.username(), "Username must match.");
                    // Check that the adapter returns the stored (hashed) password
                    assertEquals(hashedPassword, user.password(), "Password must be the hashed one.");
                    assertEquals("ROLE_ADMIN", user.role(), "Role must match.");
                })
                .verifyComplete();
    }

    @Test
    void findByUsername_shouldCompleteEmptyIfNotFound() {
        StepVerifier.create(userRepositoryAdapter.findByUsername("nonexistent"))
                .verifyComplete();
    }
}