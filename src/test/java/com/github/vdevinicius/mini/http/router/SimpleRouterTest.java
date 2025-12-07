package com.github.vdevinicius.mini.http.router;

import com.github.vdevinicius.mini.http.core.Handler;
import com.github.vdevinicius.mini.http.core.MiniHttpRequest;
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
        final var req = MiniHttpRequest.newBuilder()
                .method("GET")
                .path("/v1/handler")
                .build();
        sut.get("/v1/handler", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/handler", result.matchedUri());
    }

    @Test
    void shouldMatchDynamicGetRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("GET")
                .path("/v1/handler/1234")
                .build();
        sut.get("/v1/handler/{handler_id}", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/handler/{handler_id}", result.matchedUri());
    }

    @Test
    void whenGetHandlerIsDeclaredMoreThanOnce_thenShouldThrowUnsupportedOperationException() {
        sut.get("/v1/handler", NOOP_HANDLER);
        assertThrows(UnsupportedOperationException.class, () -> sut.get("/v1/handler", NOOP_HANDLER));
    }

    @Test
    void shouldMatchStaticPostRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("POST")
                .path("/v1/handler")
                .build();
        sut.post("/v1/handler", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/handler", result.matchedUri());
    }

    @Test
    void shouldMatchDynamicPostRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("POST")
                .path("/v1/handler/1234")
                .build();
        sut.post("/v1/handler/{handler_id}", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/handler/{handler_id}", result.matchedUri());
    }

    @Test
    void whenPostHandlerIsDeclaredMoreThanOnce_thenShouldThrowUnsupportedOperationException() {
        sut.post("/v1/handler", NOOP_HANDLER);
        assertThrows(UnsupportedOperationException.class, () -> sut.post("/v1/handler", NOOP_HANDLER));
    }

    @Test
    void shouldMatchStaticPutRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("PUT")
                .path("/v1/handler")
                .build();
        sut.put("/v1/handler", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/handler", result.matchedUri());
    }

    @Test
    void shouldMatchDynamicPutRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("PUT")
                .path("/v1/handler/1234")
                .build();
        sut.put("/v1/handler/{handler_id}", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/handler/{handler_id}", result.matchedUri());
    }

    @Test
    void whenPutHandlerIsDeclaredMoreThanOnce_thenShouldThrowUnsupportedOperationException() {
        sut.put("/v1/handler", NOOP_HANDLER);
        assertThrows(UnsupportedOperationException.class, () -> sut.put("/v1/handler", NOOP_HANDLER));
    }

    @Test
    void shouldMatchStaticPatchRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("PATCH")
                .path("/v1/handler")
                .build();
        sut.patch("/v1/handler", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/handler", result.matchedUri());
    }

    @Test
    void shouldMatchDynamicPatchRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("PATCH")
                .path("/v1/handler/1234")
                .build();
        sut.patch("/v1/handler/{handler_id}", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/handler/{handler_id}", result.matchedUri());
    }

    @Test
    void whenPatchHandlerIsDeclaredMoreThanOnce_thenShouldThrowUnsupportedOperationException() {
        sut.patch("/v1/handler", NOOP_HANDLER);
        assertThrows(UnsupportedOperationException.class, () -> sut.patch("/v1/handler", NOOP_HANDLER));
    }


    @Test
    void shouldMatchStaticDeleteRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("DELETE")
                .path("/v1/resource")
                .build();
        sut.delete("/v1/resource", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/resource", result.matchedUri());
    }

    @Test
    void shouldMatchDynamicDeleteRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("DELETE")
                .path("/v1/resource/1234")
                .build();
        sut.delete("/v1/resource/{resource_id}", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/resource/{resource_id}", result.matchedUri());
    }

    @Test
    void whenDeleteHandlerIsDeclaredMoreThanOnce_thenShouldThrowUnsupportedOperationException() {
        sut.delete("/v1/resource", NOOP_HANDLER);
        assertThrows(UnsupportedOperationException.class, () -> sut.delete("/v1/resource", NOOP_HANDLER));
    }

    @Test
    void shouldMatchStaticHeadRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("HEAD")
                .path("/v1/resource")
                .build();
        sut.head("/v1/resource", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/resource", result.matchedUri());
    }

    @Test
    void shouldMatchDynamicHeadRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("HEAD")
                .path("/v1/resource/1234")
                .build();
        sut.head("/v1/resource/{resource_id}", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/resource/{resource_id}", result.matchedUri());
    }

    @Test
    void whenHeadHandlerIsDeclaredMoreThanOnce_thenShouldThrowUnsupportedOperationException() {
        sut.head("/v1/resource", NOOP_HANDLER);
        assertThrows(UnsupportedOperationException.class, () -> sut.head("/v1/resource", NOOP_HANDLER));
    }

    @Test
    void shouldMatchStaticConnectRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("CONNECT")
                .path("/v1/resource")
                .build();
        sut.connect("/v1/resource", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/resource", result.matchedUri());
    }

    @Test
    void shouldMatchDynamicConnectRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("CONNECT")
                .path("/v1/resource/1234")
                .build();
        sut.connect("/v1/resource/{resource_id}", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/resource/{resource_id}", result.matchedUri());
    }

    @Test
    void whenConnectHandlerIsDeclaredMoreThanOnce_thenShouldThrowUnsupportedOperationException() {
        sut.connect("/v1/resource", NOOP_HANDLER);
        assertThrows(UnsupportedOperationException.class, () -> sut.connect("/v1/resource", NOOP_HANDLER));
    }

    @Test
    void shouldMatchStaticOptionsRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("OPTIONS")
                .path("/v1/resource")
                .build();
        sut.options("/v1/resource", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/resource", result.matchedUri());
    }

    @Test
    void shouldMatchDynamicOptionsRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("OPTIONS")
                .path("/v1/resource/1234")
                .build();
        sut.options("/v1/resource/{resource_id}", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/resource/{resource_id}", result.matchedUri());
    }

    @Test
    void whenOptionsHandlerIsDeclaredMoreThanOnce_thenShouldThrowUnsupportedOperationException() {
        sut.options("/v1/resource", NOOP_HANDLER);
        assertThrows(UnsupportedOperationException.class, () -> sut.options("/v1/resource", NOOP_HANDLER));
    }

    @Test
    void shouldMatchStaticTraceRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("TRACE")
                .path("/v1/resource")
                .build();
        sut.trace("/v1/resource", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/resource", result.matchedUri());
    }

    @Test
    void shouldMatchDynamicTraceRoute() throws NoHandlerFoundException {
        final var req = MiniHttpRequest.newBuilder()
                .method("TRACE")
                .path("/v1/resource/1234")
                .build();
        sut.trace("/v1/resource/{resource_id}", NOOP_HANDLER);
        final var result = sut.match(req);
        assertEquals(NOOP_HANDLER, result.handler());
        assertEquals("/v1/resource/{resource_id}", result.matchedUri());
    }

    @Test
    void whenTraceHandlerIsDeclaredMoreThanOnce_thenShouldThrowUnsupportedOperationException() {
        sut.trace("/v1/resource", NOOP_HANDLER);
        assertThrows(UnsupportedOperationException.class, () -> sut.trace("/v1/resource", NOOP_HANDLER));
    }
}
