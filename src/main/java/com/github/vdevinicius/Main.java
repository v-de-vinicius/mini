package com.github.vdevinicius;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

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
                    final var requestBytes = accumulator.toByteArray();
                    final var reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(requestBytes)));
                    reader.readLine(); // discard first line — HTTP <METHOD> / 1.1
                    final var headers = new HashMap<String, String>();
                    var header = reader.readLine();
                    while (!header.isEmpty()) {
                        final var headerFragments = header.split(":");
                        final var key = headerFragments[0].toLowerCase();
                        final var value = headerFragments[1].trim().toLowerCase();
                        if (headers.containsKey(key)) {
                            headers.put(key, headers.get(key) + ";" + value);
                        } else {
                            headers.put(key, value);
                        }
                        header = reader.readLine();
                    }
                    final var body = new ByteArrayOutputStream();
                    // Request bytes might be incomplete if request data can't fit at buffer all at once. Need to refactor to cover this scenario
                    body.write(requestBytes, bodyIndex, Integer.parseInt(headers.get("content-length")));
                    System.out.println("[mini-http] Starting reading body in position [" + bodyIndex + "]");
                    outputStream.write("HTTP 1.1 / 200 OK".getBytes(StandardCharsets.UTF_8));
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