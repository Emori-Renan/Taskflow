package com.taskflow.user.infrastructure.adapter.in.messaging;

import org.springframework.stereotype.Component;

import com.taskflow.user.application.port.out.UserProfileRepositoryPort;
import com.taskflow.user.domain.model.UserProfile;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class UserCreatedEventListener {

    private final UserProfileRepositoryPort repository;

    @SqsListener("user-created")
    public Mono<Void> handle(UserCreatedEvent event) {
        return repository.save(
            new UserProfile(event.userId(), event.email())
        ).then();
    }
}
