package com.github.vdevinicius.mini.http.exception;

import com.github.vdevinicius.mini.http.core.HttpResponseStatus;

public class InvalidHttpMessageException extends RuntimeException {
    private final HttpResponseStatus status;

    public InvalidHttpMessageException(String message, HttpResponseStatus status) {
        super(message);
        this.status = status;
    }
}
