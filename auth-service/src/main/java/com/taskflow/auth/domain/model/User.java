package com.taskflow.auth.domain.model;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record User(
        UUID id,

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Invalid email format")
        String email,

        String password,

        @NotBlank(message = "Role must not be blank")
        String role,

        String oauthProvider
    ) {
    public User(String email, String password, String role) {
        this(null, email, password, role, null);
    }

    public User(UUID id, String email, String password, String role) {
        this(id, email, password, role, null);
    }

    public static User ofOAuth(String email, String role, String oauthProvider) {
        return new User(null, email, null, role, oauthProvider);
    }
}
