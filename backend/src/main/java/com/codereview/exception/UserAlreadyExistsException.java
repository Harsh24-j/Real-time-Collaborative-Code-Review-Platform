package com.codereview.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a registration attempt detects a duplicate username or email.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
