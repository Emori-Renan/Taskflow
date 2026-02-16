package com.taskflow.auth.application.port.out;

import com.taskflow.auth.domain.event.UserCreatedEvent;

public interface EventPublisherPort {
    void publish(UserCreatedEvent event);
}
