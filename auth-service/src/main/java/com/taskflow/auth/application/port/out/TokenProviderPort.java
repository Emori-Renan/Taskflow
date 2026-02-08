package com.taskflow.auth.application.port.out;

import com.taskflow.auth.domain.model.User;

public interface TokenProviderPort {
    String generateToken(User user);
    String generateRefreshToken(User user);
    String extractEmailFromRefreshToken(String token);
    boolean isRefreshTokenValid(String token);
}