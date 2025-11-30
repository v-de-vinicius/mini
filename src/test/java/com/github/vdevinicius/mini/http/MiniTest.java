package com.github.vdevinicius.mini.http;

import com.github.vdevinicius.mini.http.core.ExceptionHandler;
import com.github.vdevinicius.mini.http.core.ExceptionHandlerMatcher;
import com.github.vdevinicius.mini.http.core.Handler;
import com.github.vdevinicius.mini.http.router.MatchingRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetSocketAddress;
import java.net.Socket;

import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MiniTest {

    @Mock
    private MatchingRouter<Mini> router;

    @Mock
    private ExceptionHandlerMatcher<Mini> exceptionHandlerMatcher;

    private Mini sut;

    @BeforeEach
    public void setUp() {
        sut = new Mini(router, exceptionHandlerMatcher);
    }

    @Test
    void shouldListenForRequestsInTheConfiguredPort() throws Exception {
        final var thread = new Thread(() -> sut.port(10000).start());
        thread.setDaemon(true);
        thread.start();
        for (int i = 0; i < 50; i++) {
            try (final var socket = new Socket()) {
                socket.connect(new InetSocketAddress("127.0.0.1", 10000), 100);
                assert true;
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
        fail("Connection timeout exceeded. Not able to connect to the server");
    }

    @Nested
    class RouterTest {
        private static final Handler NOOP_HANDLER = (req, res) -> {};

        @Test
        void shouldDelegateGetMappingToMatchingRouter() {
            sut.get("/v1/handler", NOOP_HANDLER);
            verify(router, atMostOnce()).get("/v1/handler", NOOP_HANDLER);
        }

        @Test
        void shouldDelegatePostMappingToMatchingRouter() {
            sut.post("/v1/handler", NOOP_HANDLER);
            verify(router, atMostOnce()).post("/v1/handler", NOOP_HANDLER);
        }

        @Test
        void shouldDelegatePutMappingToMatchingRouter() {
            sut.put("/v1/handler", NOOP_HANDLER);
            verify(router, atMostOnce()).put("/v1/handler", NOOP_HANDLER);
        }

        @Test
        void shouldDelegateDeleteMappingToMatchingRouter() {
            sut.delete("/v1/handler", NOOP_HANDLER);
            verify(router, atMostOnce()).delete("/v1/handler", NOOP_HANDLER);
        }

        @Test
        void shouldDelegatePatchMappingToMatchingRouter() {
            sut.patch("/v1/handler", NOOP_HANDLER);
            verify(router, atMostOnce()).patch("/v1/handler", NOOP_HANDLER);
        }

        @Test
        void shouldDelegateTraceMappingToMatchingRouter() {
            sut.trace("/v1/handler", NOOP_HANDLER);
            verify(router, atMostOnce()).trace("/v1/handler", NOOP_HANDLER);
        }

        @Test
        void shouldDelegateConnectMappingToMatchingRouter() {
            sut.connect("/v1/handler", NOOP_HANDLER);
            verify(router, atMostOnce()).connect("/v1/handler", NOOP_HANDLER);
        }

        @Test
        void shouldDelegateHeadMappingToMatchingRouter() {
            sut.head("/v1/handler", NOOP_HANDLER);
            verify(router, atMostOnce()).head("/v1/handler", NOOP_HANDLER);
        }

        @Test
        void shouldDelegateOptionsMappingToMatchingRouter() {
            sut.options("/v1/handler", NOOP_HANDLER);
            verify(router, atMostOnce()).options("/v1/handler", NOOP_HANDLER);
        }
    }

    @Nested
    class ExceptionHandlerMappingTest {

        private static final ExceptionHandler<RuntimeException> NOOP_HANDLER = (e, req, res) -> {};

        @Test
        void shouldDelegateExceptionHandlingToExceptionHandlerMatcher() {
            sut.exceptionCaught(RuntimeException.class, NOOP_HANDLER);
            verify(exceptionHandlerMatcher, atMostOnce()).exceptionCaught(RuntimeException.class, NOOP_HANDLER);
        }
    }
}