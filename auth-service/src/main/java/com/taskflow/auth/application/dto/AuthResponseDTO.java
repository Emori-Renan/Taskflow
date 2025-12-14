package com.taskflow.auth.application.dto;

public record AuthResponseDTO(String token, String email, String role) {}