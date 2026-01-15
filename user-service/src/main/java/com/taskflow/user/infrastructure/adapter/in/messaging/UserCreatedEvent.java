package com.taskflow.user.infrastructure.adapter.in.messaging;

import java.util.UUID;

public record UserCreatedEvent(UUID userId, String email) {
}
    