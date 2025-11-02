package com.taskflow.auth.domain.model;

public record User(String id, String username, String password, String role) {}