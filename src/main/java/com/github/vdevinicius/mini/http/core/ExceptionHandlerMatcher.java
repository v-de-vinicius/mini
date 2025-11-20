package com.github.vdevinicius.mini.http.core;

public interface ExceptionHandlerMatcher<T> extends ExceptionHandlerRegistry<T> {
    <E extends Throwable> ExceptionHandler<E> match(E throwable);
}
