package com.taskflow.auth.application.port.out;

import com.taskflow.auth.domain.model.User;

import reactor.core.publisher.Mono;

public interface UserRepositoryPort {
    Mono<User> findByEmail(String email);
    Mono<User> save(User user);
}