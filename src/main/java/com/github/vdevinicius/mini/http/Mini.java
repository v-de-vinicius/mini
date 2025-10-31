package com.github.vdevinicius.mini.http;

import com.github.vdevinicius.mini.http.codec.decoder.HttpMessageDecoder;
import com.github.vdevinicius.mini.http.core.Handler;
import com.github.vdevinicius.mini.http.core.HttpResponse;
import com.github.vdevinicius.mini.http.exception.NoHandlerFoundException;
import com.github.vdevinicius.mini.http.router.Router;
import com.github.vdevinicius.mini.http.router.SimpleRouter;

import java.net.ServerSocket;
import java.net.SocketTimeoutException;

public class Mini implements Router<Mini> {
    private final SimpleRouter routerDelegator = new SimpleRouter();

    private int port = 8080;

    public Mini port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public Mini get(String path, Handler handler) {
        routerDelegator.get(path, handler);
        return this;
    }

    @Override
    public Mini post(String path, Handler handler) {
        routerDelegator.post(path, handler);
        return this;
    }

    @Override
    public Mini put(String path, Handler handler) {
        routerDelegator.put(path, handler);
        return this;
    }

    @Override
    public Mini patch(String path, Handler handler) {
        routerDelegator.patch(path, handler);
        return this;
    }

    @Override
    public Mini delete(String path, Handler handler) {
        routerDelegator.delete(path, handler);
        return this;
    }

    @Override
    public Mini head(String path, Handler handler) {
        routerDelegator.head(path, handler);
        return this;
    }

    @Override
    public Mini connect(String path, Handler handler) {
        routerDelegator.connect(path, handler);
        return this;
    }

    @Override
    public Mini options(String path, Handler handler) {
        routerDelegator.options(path, handler);
        return this;
    }

    @Override
    public Mini trace(String path, Handler handler) {
        routerDelegator.trace(path, handler);
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
                    try {
                        final var request = decoder.read();
                        final var response = HttpResponse.newBuilder().build();
                        final var matchedRoute = this.routerDelegator.match(request);
                        matchedRoute.handler().handle(request, response);
                        outputStream.write(response.getBytes());
                        outputStream.flush();
                    } catch (UnsupportedOperationException e) {
                        outputStream.write(HttpResponse.newBuilder().status(501).body(e.getMessage()).build().getBytes());
                        outputStream.flush();
                    } catch (IllegalStateException e) {
                        outputStream.write(HttpResponse.newBuilder().status(400).body(e.getMessage()).build().getBytes());
                        outputStream.flush();
                    } catch (NoHandlerFoundException e) {
                        outputStream.write(HttpResponse.newBuilder().status(404).body("resource not found").build().getBytes());
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
