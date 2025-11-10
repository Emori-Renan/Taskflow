package com.taskflow.auth.domain.model;

import java.util.UUID;

public record User(UUID id, String username, String password, String role) {
    public User(String username, String password, String role) {
        this(null, username, password, role);
    }

}