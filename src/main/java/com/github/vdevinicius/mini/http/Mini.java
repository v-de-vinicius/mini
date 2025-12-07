package com.github.vdevinicius.mini.http;

import com.github.vdevinicius.mini.http.codec.HeadMessageEncoder;
import com.github.vdevinicius.mini.http.codec.HttpMessageDecoder;
import com.github.vdevinicius.mini.http.codec.HttpMessageEncoder;
import com.github.vdevinicius.mini.http.core.ExceptionHandler;
import com.github.vdevinicius.mini.http.core.ExceptionHandlerMatcher;
import com.github.vdevinicius.mini.http.core.ExceptionHandlerRegistry;
import com.github.vdevinicius.mini.http.core.Handler;
import com.github.vdevinicius.mini.http.core.HttpMethod;
import com.github.vdevinicius.mini.http.core.MiniHttpRequest;
import com.github.vdevinicius.mini.http.core.MiniHttpResponse;
import com.github.vdevinicius.mini.http.core.SimpleExceptionHandlerMatcher;
import com.github.vdevinicius.mini.http.exception.NoHandlerFoundException;
import com.github.vdevinicius.mini.http.router.MatchingRouter;
import com.github.vdevinicius.mini.http.router.Router;
import com.github.vdevinicius.mini.http.router.SimpleRouter;

import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.time.Clock;
import java.util.Map;

// TODO: Implement graceful shutdown
public final class Mini implements Router<Mini>, ExceptionHandlerRegistry<Mini> {
    private final MatchingRouter<?> router;
    private final ExceptionHandlerMatcher<?> exceptionHandlerMatcher;
    private final Map<HttpMethod, HttpMessageEncoder> encoderMap = Map.of(HttpMethod.HEAD, new HeadMessageEncoder(Clock.systemUTC()));

    private int port = 8080;

    Mini(MatchingRouter<?> router, ExceptionHandlerMatcher<?> exceptionHandlerMatcher) {
        this.router = router;
        this.exceptionHandlerMatcher = exceptionHandlerMatcher;
    }

    public static Mini newServer() {
        return new Mini(new SimpleRouter(), new SimpleExceptionHandlerMatcher());
    }

    public Mini port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public Mini get(String path, Handler handler) {
        router.get(path, handler);
        return this;
    }

    @Override
    public Mini post(String path, Handler handler) {
        router.post(path, handler);
        return this;
    }

    @Override
    public Mini put(String path, Handler handler) {
        router.put(path, handler);
        return this;
    }

    @Override
    public Mini patch(String path, Handler handler) {
        router.patch(path, handler);
        return this;
    }

    @Override
    public Mini delete(String path, Handler handler) {
        router.delete(path, handler);
        return this;
    }

    @Override
    public Mini head(String path, Handler handler) {
        router.head(path, handler);
        return this;
    }

    @Override
    public Mini connect(String path, Handler handler) {
        router.connect(path, handler);
        return this;
    }

    @Override
    public Mini options(String path, Handler handler) {
        router.options(path, handler);
        return this;
    }

    @Override
    public Mini trace(String path, Handler handler) {
        router.trace(path, handler);
        return this;
    }

    @Override
    public <E extends Throwable> Mini exceptionCaught(Class<E> exceptionClass, ExceptionHandler<E> handler) {
        exceptionHandlerMatcher.exceptionCaught(exceptionClass, handler);
        return this;
    }

    public void start() {
        try (final var serverSocket = new ServerSocket(this.port)) {
            while (true) {
                try (final var socket = serverSocket.accept()) {
                    socket.setSoTimeout(5000);
                    final var inputStream = socket.getInputStream();
                    final var outputStream = socket.getOutputStream();
                    final var decoder = new HttpMessageDecoder(inputStream, 8192);
                    final var request = decoder.read();
                    final var response = MiniHttpResponse.newBuilder().build();
                    try {
                        final var matchedRoute = this.router.match(request);
                        final var req = MiniHttpRequest.of(request, builder -> builder.matchedByRoute(matchedRoute.matchedUri()));
                        matchedRoute.handler().handle(req, response);
                        final var encoder = encoderMap.getOrDefault(request.method(), new HttpMessageEncoder() {
                            @Override
                            public byte[] encode(MiniHttpRequest req, MiniHttpResponse res) {
                                return res.getBytes();
                            }
                        });
                        outputStream.write(encoder.encode(req, response));
                        outputStream.flush();
                    } catch (UnsupportedOperationException e) {
                        outputStream.write(MiniHttpResponse.newBuilder().status(501).body(e.getMessage()).build().getBytes());
                        outputStream.flush();
                    } catch (IllegalStateException e) {
                        outputStream.write(MiniHttpResponse.newBuilder().status(400).body(e.getMessage()).build().getBytes());
                        outputStream.flush();
                    } catch (NoHandlerFoundException e) {
                        outputStream.write(MiniHttpResponse.newBuilder().status(404).body("resource not found").build().getBytes());
                        outputStream.flush();
                    } catch (Throwable t) {
                        final var handler = exceptionHandlerMatcher.match(t);
                        handler.handle(t, request, response);
                        outputStream.write(response.getBytes());
                        outputStream.flush();
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("[mini-http] Socket automatically closed after 5s of inactivity.");
                } catch (Throwable t) {
                    System.out.println("[mini-http] Unexpected error occurred: " + t.getMessage());
                }
            }
        } catch (Throwable t) {
            System.out.println("[mini-http] Could not bootstrap server: " + t.getMessage());
        }
    }
}
