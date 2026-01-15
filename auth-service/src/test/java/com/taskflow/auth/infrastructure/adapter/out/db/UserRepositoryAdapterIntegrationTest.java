package com.taskflow.auth.infrastructure.adapter.out.db;

import com.taskflow.auth.domain.model.User;
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

    private UUID insertTestUser(String email, String rawPassword, String role) {
        UUID id = UUID.randomUUID();
        String hashed = passwordEncoder.encode(rawPassword);

        databaseClient.sql("""
            INSERT INTO users (id, email, password, role)
            VALUES (:id, :email, :password, :role)
        """)
        .bind("id", id)
        .bind("email", email)
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
                "newuser@example.com",
                "rawPassword123",
                "ROLE_MEMBER"
        );

        StepVerifier.create(userRepositoryAdapter.save(rawUser))
                .assertNext(savedUser -> {
                    assertNotNull(savedUser.id(), "User ID should be generated.");
                    assertEquals("newuser@example.com", savedUser.email());
                    assertEquals("ROLE_MEMBER", savedUser.role());
                })
                .verifyComplete();

        StepVerifier.create(springDataRepository.findByEmail("newuser@example.com"))
                .assertNext(entity ->
                        assertTrue(
                                passwordEncoder.matches("rawPassword123", entity.getPassword()),
                                "Password must be hashed in DB."
                        )
                )
                .verifyComplete();
    }

    @Test
    void findByEmail_shouldReturnDomainModelIfFound() {
        insertTestUser("existinguser@example.com", "testpass", "ROLE_ADMIN");

        StepVerifier.create(userRepositoryAdapter.findByEmail("existinguser@example.com"))
                .assertNext(user -> {
                    assertNotNull(user.id());
                    assertEquals("existinguser@example.com", user.email());
                    assertEquals("ROLE_ADMIN", user.role());
                    assertTrue(passwordEncoder.matches("testpass", user.password()));
                })
                .verifyComplete();
    }

    @Test
    void findByEmail_shouldCompleteEmptyIfNotFound() {
        StepVerifier.create(userRepositoryAdapter.findByEmail("nonexistent@example.com"))
                .verifyComplete();
    }

    @Test
    void save_shouldFailOnDuplicateEmail() {
        insertTestUser("dupe@example.com", "secret", "ROLE_USER");

        User duplicate = new User(
                UUID.randomUUID(),
                "dupe@example.com",
                "anotherpass",
                "ROLE_USER"
        );

        StepVerifier.create(userRepositoryAdapter.save(duplicate))
                .expectError(DataIntegrityViolationException.class)
                .verify();
    }

    @Test
    void save_shouldFailOnNullEmail() {
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
