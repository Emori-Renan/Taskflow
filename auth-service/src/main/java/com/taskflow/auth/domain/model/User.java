package com.taskflow.auth.domain.model;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record User(
        UUID id, 

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Invalid email format")
        String email, 

        @NotBlank(message = "Password must not be blank")
        String password, 

        @NotBlank(message = "Role must not be blank")
        String role
    ) {
    public User(String email, String password, String role) {
        this(null, email, password, role);
    }

}