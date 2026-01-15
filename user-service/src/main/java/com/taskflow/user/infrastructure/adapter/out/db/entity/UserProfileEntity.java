package com.taskflow.user.infrastructure.adapter.out.db.entity;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table("user_profiles")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileEntity {

    @Id
    private UUID id;
    private String email;
    private String displayName; 
}
