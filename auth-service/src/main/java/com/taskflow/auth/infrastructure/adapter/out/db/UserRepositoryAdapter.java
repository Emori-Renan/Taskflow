package com.taskflow.auth.infrastructure.adapter.out.db;

import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final Map<String, User> users = new HashMap<>();

    public UserRepositoryAdapter() {
        // Example bcrypt hash for password "admin"
        users.put("admin", new User("1", "admin",
                "$2a$10$8Q6lzE/qz0N4SkCwF7Zz9OhT1LDY.2DkkEFrf.7WfyYqV0mUDlT8u",
                "ADMIN"));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }
}
