package com.github.vdevinicius.mini.http.exception;

import com.github.vdevinicius.mini.http.core.MiniHttpRequest;

import java.io.Serial;

public class NoHandlerFoundException extends Exception {

    @Serial
    private static final long serialVersionUID = 4817224146161094855L;

    private final MiniHttpRequest req;

    public NoHandlerFoundException(MiniHttpRequest req) {
        this.req = req;
    }

    public MiniHttpRequest request() {
        return req;
    }
}
