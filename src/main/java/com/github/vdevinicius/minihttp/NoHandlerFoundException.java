package com.github.vdevinicius.minihttp;

public class NoHandlerFoundException extends Exception {
    private final HttpMethod method;
    private final String uri;

    public NoHandlerFoundException(HttpMethod method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }
}
