package com.github.vdevinicius.mini.http.exception;

import com.github.vdevinicius.mini.http.core.HttpRequest;

import java.io.Serial;

public class NoHandlerFoundException extends Exception {

    @Serial
    private static final long serialVersionUID = 4817224146161094855L;

    private final HttpRequest req;

    public NoHandlerFoundException(HttpRequest req) {
        this.req = req;
    }

    public HttpRequest request() {
        return req;
    }
}
