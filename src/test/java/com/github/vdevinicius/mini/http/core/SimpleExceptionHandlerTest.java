package com.github.vdevinicius.mini.http.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleExceptionHandlerTest {

    private static final ExceptionHandler<RuntimeException> NOOP_HANDLER = (e, req, res) -> {};

    private SimpleExceptionHandler sut;

    @BeforeEach
    public void setUp() {
        sut = new SimpleExceptionHandler();
    }

    @Test
    void shouldHandleExceptionWithDefinedHandler() {
        sut.handleException(RuntimeException.class, NOOP_HANDLER);
        final var exception = new RuntimeException("unknown error");
        final var result = sut.resolve(exception);
        assertEquals(NOOP_HANDLER, result);
    }
}
