package com.github.vdevinicius.mini.http.core;

@FunctionalInterface
public interface ExceptionHandlerRegistry<T> {
    <E extends Throwable> T handleException(Class<E> exceptionClass, ExceptionHandler<E> handler);
}
