package com.github.vdevinicius.minihttp;

import com.github.vdevinicius.minihttp.codec.HttpMessageDecoder;
import com.github.vdevinicius.minihttp.routing.Router;
import com.github.vdevinicius.minihttp.routing.SimpleRouter;

import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.function.Consumer;

public class Mini {
    private final SimpleRouter simpleRouter = new SimpleRouter();

    private int port = 8080;

    public Mini port(int port) {
        this.port = port;
        return this;
    }

    public Mini router(Consumer<Router> consumer) {
        consumer.accept(this.simpleRouter);
        return this;
    }

    public void start() {
        try (final var serverSocket = new ServerSocket(8080)) {
            while (true) {
                try (final var socket = serverSocket.accept()) {
                    socket.setSoTimeout(5000);
                    final var inputStream = socket.getInputStream();
                    final var outputStream = socket.getOutputStream();
                    final var decoder = new HttpMessageDecoder(inputStream, 8192);
                    try {
                        final var request = decoder.read();
                        final var response = HttpResponse.newBuilder().build();
                        final var handler = this.simpleRouter.match(request);
                        handler.accept(request, response);
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
