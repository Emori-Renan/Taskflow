package com.taskflow.auth.infrastructure.adapter.out.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.taskflow.auth.infrastructure.adapter.out.db.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);
}