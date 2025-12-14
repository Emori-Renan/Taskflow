package com.taskflow.auth.infrastructure.adapter.out.db.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.taskflow.auth.infrastructure.adapter.out.db.entity.UserEntity;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SpringDataUserRepository extends ReactiveCrudRepository<UserEntity, UUID> {
    Mono<UserEntity> findByEmail(String email);
}