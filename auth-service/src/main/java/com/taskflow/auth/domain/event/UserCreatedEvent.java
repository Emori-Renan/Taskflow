package com.taskflow.auth.domain.event;

import java.util.UUID;

public record UserCreatedEvent(UUID userId, String email) {
}
