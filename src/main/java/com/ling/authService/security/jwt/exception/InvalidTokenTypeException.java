package com.ling.authService.security.jwt.exception;

public class InvalidTokenTypeException extends RuntimeException {
    public InvalidTokenTypeException(String message) {
        super(message);
    }
}
