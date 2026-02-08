package com.taskflow.auth.application.port.out;

import com.taskflow.auth.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);
    User save(User user);
}