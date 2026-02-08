package com.taskflow.auth.domain.exception;

public class InvalidTokenException extends DomainException {
    public InvalidTokenException() {
        super("Invalid or expired refresh token.");
    }
}
