package com.github.vdevinicius;

import java.io.ByteArrayOutputStream;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        try (final var serverSocket = new ServerSocket(8080)) {
            while (true) {
                try (final var socket = serverSocket.accept()) {
                    socket.setSoTimeout(5000);
                    final var buffer = new byte[8192];
                    final var inputStream = socket.getInputStream();
                    final var outputStream = socket.getOutputStream();
                    final var accumulator = new ByteArrayOutputStream();
                    var bodyIndex = -1;
                    while (bodyIndex == -1) {
                        accumulator.write(buffer, 0, inputStream.read(buffer));
                        bodyIndex = getBodyIndex(accumulator.toByteArray(), accumulator.size());
                    }
                    System.out.println("[mini-http] Starting reading body in position [" + bodyIndex + "]");
                    outputStream.write("Request readed sucessfully.".getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                } catch (SocketTimeoutException e) {
                    System.out.println("[mini-http] Socket automatically closed after 5s of inactivity.");
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static int getBodyIndex(final byte[] content, final int len) {
        if (len < 4) return -1;

        for (int i = 0; i <= len - 4; i++) {
            if (content[i] == 13 && content[i+1] == 10 && content[i+2] == 13 && content[i+3] == 10) {
                return i + 4;
            }
        }

        return -1;
    }
}