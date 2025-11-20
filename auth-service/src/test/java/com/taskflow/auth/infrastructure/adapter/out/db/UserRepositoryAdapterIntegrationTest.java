package com.taskflow.auth.infrastructure.adapter.out.db;

import com.taskflow.auth.domain.model.User;
import com.taskflow.auth.infrastructure.adapter.out.db.repository.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// Configure the test to run within a Spring Data JPA context
@DataJpaTest
// Import the adapter and its required dependencies (PasswordEncoder)
@Import({UserRepositoryAdapter.class, BCryptPasswordEncoder.class}) 
class UserRepositoryAdapterIntegrationTest {

    // The adapter we are testing
    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    // The underlying repository (used for verification/setup only)
    @Autowired
    private SpringDataUserRepository springDataRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void save_shouldPersistUserAndHashPassword() {
        // Arrange: The User domain object has a raw password
        User rawUser = new User(null, "newuser", "rawPassword123", "ROLE_MEMBER");

        // Act
        User savedUser = userRepositoryAdapter.save(rawUser);

        // Assert: 1. The domain object mapping is correct
        assertNotNull(savedUser.id(), "ID should be generated.");
        assertEquals(rawUser.username(), savedUser.username());
        assertEquals(rawUser.role(), savedUser.role());
        
        // Assert: 2. The entity in the DB has a HASHED password
        var entity = springDataRepository.findByUsername(rawUser.username());
        assertTrue(entity.isPresent());
        assertTrue(passwordEncoder.matches("rawPassword123", entity.get().getPassword()), 
                   "Password must be hashed correctly in the database.");
    }

    @Test
    void findByUsername_shouldReturnDomainModelIfFound() {
        // Arrange: Manually save an entity (with a pre-hashed password)
        String rawPassword = "testpass";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        // This simulates a user already existing in the database
        springDataRepository.save(new com.taskflow.auth.infrastructure.adapter.out.db.entity.UserEntity(
                "existinguser", hashedPassword, "ROLE_ADMIN"));

        // Act
        Optional<User> foundUser = userRepositoryAdapter.findByUsername("existinguser");

        // Assert
        assertTrue(foundUser.isPresent(), "User should be found.");
        User user = foundUser.get();
        assertEquals("existinguser", user.username());
        // The adapter should return the HASHED password from the DB in the domain model
        assertEquals(hashedPassword, user.password(), "Password in domain model should be the hashed one from DB.");
        assertNotNull(user.id());
    }

    @Test
    void findByUsername_shouldReturnEmptyOptionalIfNotFound() {
        // Act
        Optional<User> foundUser = userRepositoryAdapter.findByUsername("nonexistent");

        // Assert
        assertTrue(foundUser.isEmpty(), "Optional should be empty for a nonexistent user.");
    }
}