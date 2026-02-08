package com.taskflow.auth.application.dto;

public record AuthResponseDTO(String accessToken, String refreshToken, String email, String role) {}
