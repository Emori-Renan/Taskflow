package com.taskflow.auth.domain.exception;

public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException(String username) {
        super("Username " + username + " already exists.");
    }
}