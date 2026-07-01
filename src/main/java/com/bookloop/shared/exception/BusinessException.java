package com.bookloop.shared.exception;

/**
 * Raised when a domain rule is violated (e.g. renting your own book). -> HTTP 409.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
