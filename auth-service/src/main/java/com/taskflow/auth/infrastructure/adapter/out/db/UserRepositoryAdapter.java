package com.taskflow.auth.infrastructure.adapter.out.db;

import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.model.User;
import com.taskflow.auth.infrastructure.adapter.out.db.entity.UserEntity;
import com.taskflow.auth.infrastructure.adapter.out.db.repository.SpringDataUserRepository;

import reactor.core.publisher.Mono;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserRepositoryAdapter(SpringDataUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<User> findByUsername(String username) {
        return repository.findByUsername(username)
                .map(this::toDomainModel);
    }

    @Override
    public Mono<User> save(User user) {
        UserEntity entityToSave = toPersistenceEntity(user);
        return repository.save(entityToSave).map(this::toDomainModel);
    }

    private User toDomainModel(UserEntity entity) {
        return new User(entity.getId(), entity.getUsername(), entity.getPassword(), entity.getRole());
    }
    
    private UserEntity toPersistenceEntity(User user) {
        String hashedPassword = passwordEncoder.encode(user.password());
        return new UserEntity(user.username(), hashedPassword, user.role());
    }

}
