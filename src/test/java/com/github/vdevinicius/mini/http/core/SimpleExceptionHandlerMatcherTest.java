package com.github.vdevinicius.mini.http.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleExceptionHandlerMatcherTest {

    private static final ExceptionHandler<RuntimeException> NOOP_HANDLER = (e, req, res) -> {};

    private SimpleExceptionHandlerMatcher sut;

    @BeforeEach
    public void setUp() {
        sut = new SimpleExceptionHandlerMatcher();
    }

    @Test
    void shouldExceptionCaughtWithDefinedHandler() {
        sut.exceptionCaught(RuntimeException.class, NOOP_HANDLER);
        final var exception = new RuntimeException("unknown error");
        final var result = sut.match(exception);
        assertEquals(NOOP_HANDLER, result);
    }
}
