package com.github.vdevinicius.mini.http.core;

public interface ExceptionHandlerResolver {
    <T extends Throwable> ExceptionHandler<T> resolve(T throwable);
}
