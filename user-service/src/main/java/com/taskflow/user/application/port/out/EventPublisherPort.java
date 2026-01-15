package com.taskflow.user.application.port.out;

import reactor.core.publisher.Mono;

public interface EventPublisherPort {
    Mono<Void> publish(Object event);
}
