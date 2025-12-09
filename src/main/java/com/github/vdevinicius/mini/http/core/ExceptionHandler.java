package com.github.vdevinicius.mini.http.core;

@FunctionalInterface
public interface ExceptionHandler<T extends Throwable> {
    void handle(T throwable, HttpRequest req, HttpResponse res);
}
