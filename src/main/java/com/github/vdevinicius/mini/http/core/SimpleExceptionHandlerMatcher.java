package com.github.vdevinicius.mini.http.core;

import java.util.HashMap;
import java.util.Map;

public class SimpleExceptionHandlerMatcher implements ExceptionHandlerMatcher<SimpleExceptionHandlerMatcher> {

    private final static ExceptionHandler<Throwable> DEFAULT_HANDLER = (throwable, req, res) -> {
        res.setHeader("Content-Type", "application/json");
        res.status(500);
        res.body("{ \"error\": \"%s\", \"timestamp\": \"%d\"}".formatted(throwable.getMessage(), System.currentTimeMillis()));
    };

    private final Map<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>> handlers = new HashMap<>();

    @Override
    public <T extends Throwable> SimpleExceptionHandlerMatcher exceptionCaught(Class<T> clazz, ExceptionHandler<T> handler) {
        handlers.put(clazz, handler);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Throwable> ExceptionHandler<T> match(T throwable) {
        return (ExceptionHandler<T>) handlers.getOrDefault(throwable.getClass(), DEFAULT_HANDLER);
    }
}
