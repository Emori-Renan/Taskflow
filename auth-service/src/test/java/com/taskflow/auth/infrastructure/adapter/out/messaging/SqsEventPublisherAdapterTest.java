package com.taskflow.auth.infrastructure.adapter.out.messaging;

import com.taskflow.auth.domain.event.UserCreatedEvent;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SqsEventPublisherAdapterTest {

    @Mock
    private SqsTemplate sqsTemplate;

    @InjectMocks
    private SqsEventPublisherAdapter sqsEventPublisherAdapter;

    @Test
    void publish_shouldSendEventToCorrectQueue() {
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(userId, "user@example.com");

        sqsEventPublisherAdapter.publish(event);

        verify(sqsTemplate).send("user-created", event);
    }
}
