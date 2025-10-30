package com.github.vdevinicius.minihttp;

import com.github.vdevinicius.minihttp.codec.HttpMessageDecoder;

import java.io.*;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

public class Server {
    private static final int MAX_BODY_SIZE = 1024 * 1024;

    public static void main(String[] args) {
        try (final var serverSocket = new ServerSocket(8080)) {
            while (true) {
                try (final var socket = serverSocket.accept()) {
                    socket.setSoTimeout(5000);
                    final var buffer = new byte[8192];
                    final var inputStream = socket.getInputStream();
                    final var outputStream = socket.getOutputStream();
                    final var decoder = new HttpMessageDecoder(inputStream, 8192);
                    final var request = decoder.read();
                    final var headers = request.headers();
                    final var bodyReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request.body())));
                    final var stringBuilder = new StringBuilder();
                    var bodyLine = bodyReader.readLine();
                    while (bodyLine != null && !bodyLine.isEmpty()) {
                        stringBuilder.append(bodyLine);
                        bodyLine = bodyReader.readLine();
                    }
                    final var body = stringBuilder.toString();
                    outputStream.write(Response.builder().status(200).body(body).build().getBytes());
                    outputStream.flush();
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