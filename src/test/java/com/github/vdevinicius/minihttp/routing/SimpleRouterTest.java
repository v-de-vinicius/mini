package com.github.vdevinicius.minihttp.routing;

import com.github.vdevinicius.minihttp.HttpRequest;
import com.github.vdevinicius.minihttp.HttpResponse;
import com.github.vdevinicius.minihttp.NoHandlerFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleRouterTest {

    private static final BiConsumer<HttpRequest, HttpResponse> NOOP_HANDLER = (req, res) -> {};

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
