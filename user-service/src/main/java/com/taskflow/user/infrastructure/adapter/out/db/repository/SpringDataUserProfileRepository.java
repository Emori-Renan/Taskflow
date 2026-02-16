package com.taskflow.user.infrastructure.adapter.out.db.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskflow.user.infrastructure.adapter.out.db.entity.UserProfileEntity;

public interface SpringDataUserProfileRepository
        extends JpaRepository<UserProfileEntity, UUID> {
}
