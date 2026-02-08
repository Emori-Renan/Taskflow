package com.taskflow.auth.application.port.out;

public interface RefreshTokenStoragePort {
    void store(String email, String token, long ttlMillis);
    String get(String email);
    void delete(String email);
}
