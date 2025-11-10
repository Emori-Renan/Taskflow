package com.taskflow.auth.application.port.out;

import java.util.Optional;

import com.taskflow.auth.domain.model.User;

public interface UserRepositoryPort {
    Optional<User> findByUsername(String username);
    User save(User user);
}