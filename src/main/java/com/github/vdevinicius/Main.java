package com.github.vdevinicius;

import java.io.*;
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
                    final var headerAccumulator = new ByteArrayOutputStream();
                    var bodyIndex = -1;
                    while (bodyIndex == -1) {
                        final var n = inputStream.read(buffer);
                        if (n == -1) {
                            throw new IOException("EOF found before the end of headers.");
                        }

                        headerAccumulator.write(buffer, 0, n);

                        if (headerAccumulator.size() > 32768) {
                            throw new RuntimeException("Header size greater than 32KiB");
                        }
                        bodyIndex = getBodyIndex(headerAccumulator.toByteArray(), headerAccumulator.size());
                    }
                    final var requestBytes = headerAccumulator.toByteArray();
                    final var headerReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(requestBytes)));
                    headerReader.readLine(); // discard first line — HTTP <METHOD> / 1.1
                    final var headers = new HashMap<String, String>();
                    var header = headerReader.readLine();
                    while (!header.isEmpty()) {
                        final var headerFragments = header.split(":");
                        final var key = headerFragments[0].toLowerCase();
                        final var value = headerFragments[1].trim().toLowerCase();
                        if (headers.containsKey(key)) {
                            headers.put(key, headers.get(key) + ";" + value);
                        } else {
                            headers.put(key, value);
                        }
                        header = headerReader.readLine();
                    }
                    final var body = new ByteArrayOutputStream();
                    final var already = headerAccumulator.size() - bodyIndex;
                    if (already > 0) {
                        body.write(requestBytes, bodyIndex, already);
                    }
                    var remaining = Integer.parseInt(headers.get("content-length")) - already;
                    while (remaining > 0) {
                        final var n = inputStream.read(buffer, 0, Math.min(buffer.length, remaining));
                        body.write(buffer, 0, n);
                        remaining -= n;
                    }
                    System.out.println("[mini-http] Starting reading body in position [" + bodyIndex + "]");
                    outputStream.write("HTTP 1.1 / 200 OK\r\nContent-Length: 0".getBytes(StandardCharsets.UTF_8));
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