package com.taskflow.user.application.port.out;

public interface EventPublisherPort {
    void publish(Object event);
}
