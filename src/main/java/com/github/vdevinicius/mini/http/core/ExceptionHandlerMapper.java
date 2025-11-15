package com.github.vdevinicius.mini.http.core;

@FunctionalInterface
public interface ExceptionHandlerMapper<T> {
    <E extends Throwable> T exceptionCaught(Class<E> exceptionClass, ExceptionHandler<E> handler);
}
