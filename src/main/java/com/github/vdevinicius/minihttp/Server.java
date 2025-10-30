package com.github.vdevinicius.minihttp;

import com.github.vdevinicius.minihttp.codec.HttpMessageDecoder;

import java.net.ServerSocket;
import java.net.SocketTimeoutException;

public class Server {
    public static void main(String[] args) {
        try (final var serverSocket = new ServerSocket(8080)) {
            while (true) {
                try (final var socket = serverSocket.accept()) {
                    socket.setSoTimeout(5000);
                    final var inputStream = socket.getInputStream();
                    final var outputStream = socket.getOutputStream();
                    final var decoder = new HttpMessageDecoder(inputStream, 8192);
                    try {
                        final var request = decoder.read();
                        outputStream.write(HttpResponse.newBuilder().status(200).body(request.getBodyAsString()).build().getBytes());
                        outputStream.flush();
                    } catch (UnsupportedOperationException e) {
                        outputStream.write(HttpResponse.newBuilder().status(501).body(e.getMessage()).build().getBytes());
                        outputStream.flush();
                    } catch (IllegalStateException e) {
                        outputStream.write(HttpResponse.newBuilder().status(400).body(e.getMessage()).build().getBytes());
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