package com.bookloop.shared.exception;

/** -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Object id) {
        super("%s não encontrado(a): %s".formatted(resource, id));
    }
}
