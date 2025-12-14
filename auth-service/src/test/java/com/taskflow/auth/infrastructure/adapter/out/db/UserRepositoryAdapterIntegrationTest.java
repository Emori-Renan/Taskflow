package com.taskflow.auth.infrastructure.adapter.out.db;

import com.taskflow.auth.domain.model.User;
import com.taskflow.auth.infrastructure.adapter.out.db.entity.UserEntity;
import com.taskflow.auth.infrastructure.adapter.out.db.repository.SpringDataUserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataR2dbcTest
@Import({ UserRepositoryAdapter.class, BCryptPasswordEncoder.class })
class UserRepositoryAdapterIntegrationTest {

    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    @Autowired
    private SpringDataUserRepository springDataRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DatabaseClient databaseClient;

    private UUID insertTestUser(String username, String rawPassword, String role) {
        UUID id = UUID.randomUUID();
        String hashed = passwordEncoder.encode(rawPassword);

        databaseClient.sql("""
            INSERT INTO users (id, username, password, role)
            VALUES (:id, :username, :password, :role)
        """)
        .bind("id", id)
        .bind("username", username)
        .bind("password", hashed)
        .bind("role", role)
        .fetch()
        .rowsUpdated()
        .block();

        return id;
    }

    @Test
    void save_shouldPersistUserAndHashPassword() {
        User rawUser = new User(
                UUID.randomUUID(),
                "newuser",
                "rawPassword123",
                "ROLE_MEMBER"
        );

        StepVerifier.create(userRepositoryAdapter.save(rawUser))
                .assertNext(savedUser -> {
                    assertNotNull(savedUser.id(), "User ID should be generated.");
                    assertEquals("newuser", savedUser.username());
                    assertEquals("ROLE_MEMBER", savedUser.role());
                })
                .verifyComplete();

        StepVerifier.create(springDataRepository.findByUsername("newuser"))
                .assertNext(entity ->
                        assertTrue(
                                passwordEncoder.matches("rawPassword123", entity.getPassword()),
                                "Password must be hashed in DB."
                        )
                )
                .verifyComplete();
    }

    @Test
    void findByUsername_shouldReturnDomainModelIfFound() {
        insertTestUser("existinguser", "testpass", "ROLE_ADMIN");

        StepVerifier.create(userRepositoryAdapter.findByUsername("existinguser"))
                .assertNext(user -> {
                    assertNotNull(user.id());
                    assertEquals("existinguser", user.username());
                    assertEquals("ROLE_ADMIN", user.role());
                    assertTrue(passwordEncoder.matches("testpass", user.password()));
                })
                .verifyComplete();
    }

    @Test
    void findByUsername_shouldCompleteEmptyIfNotFound() {
        StepVerifier.create(userRepositoryAdapter.findByUsername("nonexistent"))
                .verifyComplete();
    }

    @Test
    void save_shouldFailOnDuplicateUsername() {
        insertTestUser("dupeuser", "secret", "ROLE_USER");

        User duplicate = new User(
                UUID.randomUUID(),
                "dupeuser",
                "anotherpass",
                "ROLE_USER"
        );

        StepVerifier.create(userRepositoryAdapter.save(duplicate))
                .expectError(DataIntegrityViolationException.class)
                .verify();
    }

    @Test
    void save_shouldFailOnNullUsername() {
        User invalidUser = new User(
                UUID.randomUUID(),
                null,
                "pass",
                "ROLE_USER"
        );

        StepVerifier.create(userRepositoryAdapter.save(invalidUser))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
