package com.taskflow.auth.infrastructure.adapter.out.db;

import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.model.User;
import com.taskflow.auth.infrastructure.adapter.out.db.entity.UserEntity;
import com.taskflow.auth.infrastructure.adapter.out.db.repository.SpringDataUserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserRepositoryAdapter(SpringDataUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(this::toDomainModel);
    }

    @Override
    public User save(User user) {
        if (user.email() == null || user.email().isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }

        // Hash password if not already hashed
        String hashedPassword = passwordEncoder.encode(user.password());

        // Set ID to null to let JPA generate it (for new entities)
        UserEntity entity = new UserEntity(
            null,
            user.email(),
            hashedPassword,
            user.role()
        );

        UserEntity saved = repository.save(entity);
        return toDomainModel(saved);
    }

    private User toDomainModel(UserEntity entity) {
        return new User(
            entity.getId(),
            entity.getEmail(),
            entity.getPassword(),
            entity.getRole()
        );
    }

}
