package com.github.vdevinicius.mini.http.core;

public interface ExceptionHandlerRegistry<T> {
    <E extends Throwable> T exceptionCaught(Class<E> exceptionClass, ExceptionHandler<E> handler);
}
