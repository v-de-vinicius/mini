package com.github.vdevinicius.minihttp;

public class InvalidHttpMessageException extends RuntimeException {
    private final HttpResponseStatus status;

    public InvalidHttpMessageException(String message, HttpResponseStatus status) {
        super(message);
        this.status = status;
    }
}
