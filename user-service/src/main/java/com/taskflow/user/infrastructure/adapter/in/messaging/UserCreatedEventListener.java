package com.taskflow.user.infrastructure.adapter.in.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.taskflow.user.application.port.in.CreateUserProfileUseCase;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserCreatedEventListener.class);

    private final CreateUserProfileUseCase createUserProfileUseCase;

    @SqsListener("user-created")
    public void handle(UserCreatedEvent event) {
        log.info("Received UserCreatedEvent: userId={}, email={}", event.userId(), event.email());
        createUserProfileUseCase.create(event.userId(), event.email());
    }
}
