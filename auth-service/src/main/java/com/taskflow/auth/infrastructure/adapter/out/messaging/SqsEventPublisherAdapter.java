package com.taskflow.auth.infrastructure.adapter.out.messaging;

import com.taskflow.auth.application.port.out.EventPublisherPort;
import com.taskflow.auth.domain.event.UserCreatedEvent;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SqsEventPublisherAdapter implements EventPublisherPort {

    private static final Logger logger = LoggerFactory.getLogger(SqsEventPublisherAdapter.class);

    private final SqsTemplate sqsTemplate;

    public SqsEventPublisherAdapter(SqsTemplate sqsTemplate) {
        this.sqsTemplate = sqsTemplate;
    }

    @Override
    public void publish(UserCreatedEvent event) {
        logger.info("Publishing UserCreatedEvent for user: {}", event.email());
        sqsTemplate.send("user-created", event);
    }
}
