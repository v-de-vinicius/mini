package com.github.vdevinicius.mini.http.core;

import java.util.HashMap;
import java.util.Map;

public class SimpleExceptionHandler implements ExceptionHandlerRegistry<SimpleExceptionHandler>, ExceptionHandlerResolver {

    private final static ExceptionHandler<Throwable> DEFAULT_HANDLER = (throwable, req, res) -> {
        res.setBody("{ \"error\": \"%s\", \"timestamp\": \"%d\"}".formatted(throwable.getMessage(), System.currentTimeMillis()));
        res.setStatus(500);
    };

    private final Map<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>> handlers = new HashMap<>();

    @Override
    public <E extends Throwable> SimpleExceptionHandler handleException(Class<E> clazz, ExceptionHandler<E> handler) {
        handlers.put(clazz, handler);
        return this;
    }

    @Override
    public <T extends Throwable> ExceptionHandler<? extends Throwable> resolve(T throwable) {
        return handlers.getOrDefault(throwable.getClass(), DEFAULT_HANDLER);
    }
}
