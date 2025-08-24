package com.github.vdevinicius;

import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) {
        try (final var serverSocket = new ServerSocket(8080)) {
            while (true) {
                try (final var socket = serverSocket.accept()) {
                    socket.setSoTimeout(5000);
                    final var buffer = new byte[1024];
                    final var inputStream = socket.getInputStream();
                    final var outputStream = socket.getOutputStream();
                    var content = inputStream.read(buffer);
                    while (content != -1) {
                        outputStream.write(buffer, 0, content);
                        content = inputStream.read(buffer);
                    }
                    outputStream.flush();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}