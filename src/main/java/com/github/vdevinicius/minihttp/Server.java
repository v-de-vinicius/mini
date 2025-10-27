package com.github.vdevinicius.minihttp;

import java.io.*;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

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
                    final var headerAccumulator = new ByteArrayOutputStream();
                    var bodyIndex = -1;
                    while (bodyIndex == -1) {
                        final var n = inputStream.read(buffer);
                        if (n == -1) {
                            outputStream.write(Response.builder().status(400).build().getBytes());
                            outputStream.flush();
                            continue;
                        }

                        headerAccumulator.write(buffer, 0, n);

                        if (headerAccumulator.size() > 32768) {
                            outputStream.write(Response.builder().status(431).build().getBytes());
                            outputStream.flush();
                            continue;
                        }
                        bodyIndex = getBodyIndex(headerAccumulator.toByteArray(), headerAccumulator.size());
                    }
                    final var requestBytes = headerAccumulator.toByteArray();
                    final var headerBytes = new ByteArrayInputStream(requestBytes, 0, bodyIndex);
                    final var headerReader = new BufferedReader(new InputStreamReader(headerBytes, StandardCharsets.US_ASCII));
                    final var requestLine = headerReader.readLine();
                    final var method = requestLine.split(" ")[0];
                    final var headers = new HashMap<String, String>();
                    var header = headerReader.readLine();
                    while (!header.isEmpty()) {
                        final var headerFragments = header.split(":", 2);
                        final var key = headerFragments[0].trim().toLowerCase();
                        final var value = headerFragments.length > 1 ? headerFragments[1].trim() : "";
                        // Set-Cookie header must be treated separately according to RFC 6265 - So we're supporting it as a single value header.
                        if (headers.containsKey(key) && !key.equals("set-cookie")) {
                            headers.put(key, headers.get(key) + "," + value);
                        } else {
                            headers.put(key, value);
                        }
                        header = headerReader.readLine();
                    }

                    if (headers.containsKey("transfer-encoding")) {
                        outputStream.write(Response.builder().status(501).build().getBytes());
                        outputStream.flush();
                        continue;
                    }

                    if ("100-continue".equals(headers.get("expect"))) {
                        outputStream.write(Response.builder().status(501).build().getBytes());
                        outputStream.flush();
                        continue;
                    }

                    if (!headers.containsKey("host")) {
                        outputStream.write(Response.builder().status(400).build().getBytes());
                        outputStream.flush();
                        continue;
                    }


                    var contentLength = 0;
                    if (!"GET".equals(method)) {
                        // Body and content-length are relative to the request method - some methods might not contain it.
                        try {
                            if (headers.containsKey("content-length")) {
                                contentLength = Integer.parseInt(headers.get("content-length"));
                            }
                        } catch (NumberFormatException e) {
                            outputStream.write(Response.builder().status(400).build().getBytes());
                            outputStream.flush();
                            continue;
                        }

                    }

                    if (contentLength < 0) {
                        outputStream.write(Response.builder().status(400).build().getBytes());
                        outputStream.flush();
                        continue;
                    }

                    final var bodyAccumulator = new ByteArrayOutputStream();
                    final var already = headerAccumulator.size() - bodyIndex;
                    if (already > 0) {
                        bodyAccumulator.write(requestBytes, bodyIndex, already);
                    }
                    var remaining = contentLength - already;
                    while (remaining > 0) {
                        final var n = inputStream.read(buffer, 0, Math.min(buffer.length, remaining));
                        if (n == -1) {
                            outputStream.write(Response.builder().status(400).build().getBytes());
                            outputStream.flush();
                        }
                        bodyAccumulator.write(buffer, 0, n);

                        if (bodyAccumulator.size() > MAX_BODY_SIZE) {
                            outputStream.write(Response.builder().status(413).build().getBytes());
                            outputStream.flush();
                            continue;
                        }
                        remaining -= n;
                    }
                    outputStream.write(Response.builder().status(200).body(bodyAccumulator.toString()).build().getBytes());
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

    private static int getBodyIndex(final byte[] content, final int len) {
        if (len < 4) return -1;

        for (int i = 0; i <= len - 4; i++) {
            if (content[i] == 13 && content[i + 1] == 10 && content[i + 2] == 13 && content[i + 3] == 10) {
                return i + 4;
            }
        }

        return -1;
    }
}