package com.github.vdevinicius.mini.http.router;

import com.github.vdevinicius.mini.http.core.Handler;
import com.github.vdevinicius.mini.http.core.HttpRequest;
import com.github.vdevinicius.mini.http.exception.NoHandlerFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleRouterTest {

    private static final Handler NOOP_HANDLER = (req, res) -> {};

    private SimpleRouter sut;

    @BeforeEach
    public void setUp() {
        this.sut = new SimpleRouter();
    }

    @Test
    void shouldMatchStaticGetRoute() throws NoHandlerFoundException {
        final var req = HttpRequest.newBuilder()
                .method("GET")
                .uri("/v1/handler")
                .build();
        sut.get("/v1/handler", NOOP_HANDLER);
        assertEquals(NOOP_HANDLER, sut.match(req));
    }

    @Test
    void shouldMatchDynamicGetRoute() throws NoHandlerFoundException {
        final var req = HttpRequest.newBuilder()
                .method("GET")
                .uri("/v1/handler/{handler_id}")
                .build();
        sut.get("/v1/handler/1234", NOOP_HANDLER);
        assertEquals(NOOP_HANDLER, sut.match(req));
    }

    @Test
    void whenGetHandlerIsDeclaredMoreThanOnce_thenShouldThrowUnsupportedOperationException() {
        final var req = HttpRequest.newBuilder()
                .method("GET")
                .uri("/v1/handler/{handler_id}")
                .build();
        sut.get("/v1/handler", NOOP_HANDLER);
        assertThrows(UnsupportedOperationException.class, () -> sut.get("/v1/handler", NOOP_HANDLER));
    }

    @Test
    void shouldMatchStaticPostRoute() throws NoHandlerFoundException {
        final var req = HttpRequest.newBuilder()
                .method("POST")
                .uri("/v1/handler")
                .build();
        sut.post("/v1/handler", NOOP_HANDLER);
        assertEquals(NOOP_HANDLER, sut.match(req));
    }
}
