package com.taskflow.auth.domain.exception;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(String userId) {
        super("User with ID " + userId + " not found. ");
    }
}
