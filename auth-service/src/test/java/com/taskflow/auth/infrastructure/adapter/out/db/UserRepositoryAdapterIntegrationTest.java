package com.taskflow.auth.infrastructure.adapter.out.db;

import com.taskflow.auth.domain.model.User;
import com.taskflow.auth.infrastructure.adapter.out.db.entity.UserEntity;
import com.taskflow.auth.infrastructure.adapter.out.db.repository.SpringDataUserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.PersistenceException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@Import({ UserRepositoryAdapter.class, BCryptPasswordEncoder.class })
class UserRepositoryAdapterIntegrationTest {

    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    @Autowired
    private SpringDataUserRepository springDataRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestEntityManager entityManager;

    private UUID insertTestUser(String email, String rawPassword, String role) {
        String hashed = passwordEncoder.encode(rawPassword);
        UserEntity entity = new UserEntity(null, email, hashed, role);

        entityManager.persist(entity);
        entityManager.flush();

        return entity.getId();
    }

    @Test
    void save_shouldPersistUserAndHashPassword() {
        User rawUser = new User(
                UUID.randomUUID(),
                "newuser@example.com",
                "rawPassword123",
                "ROLE_MEMBER"
        );

        User savedUser = userRepositoryAdapter.save(rawUser);

        assertNotNull(savedUser.id(), "User ID should be generated.");
        assertEquals("newuser@example.com", savedUser.email());
        assertEquals("ROLE_MEMBER", savedUser.role());

        // Verify password is hashed in DB
        Optional<UserEntity> entity = springDataRepository.findByEmail("newuser@example.com");
        assertTrue(entity.isPresent());
        assertTrue(
                passwordEncoder.matches("rawPassword123", entity.get().getPassword()),
                "Password must be hashed in DB."
        );
    }

    @Test
    void findByEmail_shouldReturnDomainModelIfFound() {
        insertTestUser("existinguser@example.com", "testpass", "ROLE_ADMIN");

        Optional<User> user = userRepositoryAdapter.findByEmail("existinguser@example.com");

        assertTrue(user.isPresent());
        assertNotNull(user.get().id());
        assertEquals("existinguser@example.com", user.get().email());
        assertEquals("ROLE_ADMIN", user.get().role());
        assertTrue(passwordEncoder.matches("testpass", user.get().password()));
    }

    @Test
    void findByEmail_shouldReturnEmptyOptionalIfNotFound() {
        Optional<User> user = userRepositoryAdapter.findByEmail("nonexistent@example.com");
        assertFalse(user.isPresent());
    }

    @Test
    void save_shouldFailOnDuplicateEmail() {
        insertTestUser("dupe@example.com", "secret", "ROLE_USER");
        entityManager.clear(); // Clear persistence context to ensure constraint is checked

        User duplicate = new User(
                UUID.randomUUID(),
                "dupe@example.com",
                "anotherpass",
                "ROLE_USER"
        );

        // Expect PersistenceException (parent of Hibernate's ConstraintViolationException)
        assertThrows(PersistenceException.class,
                () -> {
                    userRepositoryAdapter.save(duplicate);
                    entityManager.flush(); // Force flush to trigger constraint violation
                });
    }

    @Test
    void save_shouldFailOnNullEmail() {
        User invalidUser = new User(
                UUID.randomUUID(),
                null,
                "pass",
                "ROLE_USER"
        );

        assertThrows(IllegalArgumentException.class,
                () -> userRepositoryAdapter.save(invalidUser));
    }
}
