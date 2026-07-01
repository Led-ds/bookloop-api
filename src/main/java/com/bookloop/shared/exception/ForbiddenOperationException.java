package com.bookloop.shared.exception;

/** Authenticated but not allowed to act on this resource. -> HTTP 403. */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
