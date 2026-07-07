package com.bookloop.shared.exception;

/**
 * Conflito de estado (HTTP 409) com um código estável para o cliente ramificar
 * (exposto em data.code do envelope). Ex.: "BOOK_ALREADY_RESERVED".
 */
public class ConflictException extends RuntimeException {

    private final String code;

    public ConflictException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
