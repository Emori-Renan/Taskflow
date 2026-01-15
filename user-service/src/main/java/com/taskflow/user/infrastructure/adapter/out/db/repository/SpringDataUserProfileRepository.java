package com.taskflow.user.infrastructure.adapter.out.db.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.taskflow.user.infrastructure.adapter.out.db.entity.UserProfileEntity;

public interface SpringDataUserProfileRepository
        extends ReactiveCrudRepository<UserProfileEntity, UUID> {
}


