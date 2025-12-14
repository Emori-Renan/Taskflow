package com.taskflow.auth.infrastructure.adapter.out.db;

import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.model.User;
import com.taskflow.auth.infrastructure.adapter.out.db.entity.UserEntity;
import com.taskflow.auth.infrastructure.adapter.out.db.repository.SpringDataUserRepository;

import reactor.core.publisher.Mono;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final DatabaseClient databaseClient;

    public UserRepositoryAdapter(SpringDataUserRepository repository, PasswordEncoder passwordEncoder,
            DatabaseClient databaseClient) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<User> findByUsername(String username) {
        return repository.findByUsername(username)
                .map(this::toDomainModel);
    }

    @Override
    public Mono<User> save(User user) {
        UUID id = user.id() != null ? user.id() : UUID.randomUUID();
        if (user.username() == null) {
            return Mono.error(new IllegalArgumentException("Username must not be null"));
        }
        String hashedPassword = passwordEncoder.encode(user.password());

        return databaseClient.sql("""
                    INSERT INTO users (id, username, password, role)
                    VALUES (:id, :username, :password, :role)
                """)
                .bind("id", id)
                .bind("username", user.username())
                .bind("password", hashedPassword)
                .bind("role", user.role())
                .fetch()
                .rowsUpdated()
                .flatMap(rows -> {
                    if (rows == 1) {
                        return Mono.just(new User(id, user.username(), hashedPassword, user.role()));
                    } else {
                        return Mono.error(new IllegalStateException("Failed to insert user"));
                    }
                });
    }

    private User toDomainModel(UserEntity entity) {
        return new User(entity.getId(), entity.getUsername(), entity.getPassword(), entity.getRole());
    }

}
