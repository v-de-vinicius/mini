package com.github.vdevinicius.minihttp;

import java.io.*;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import static java.time.format.DateTimeFormatter.*;

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
                            throw new IOException("EOF found before the end of headers.");
                        }

                        headerAccumulator.write(buffer, 0, n);

                        if (headerAccumulator.size() > 32768) {
                            // Write body back in the response
                            outputStream.write("HTTP/1.1 400 Bad Request\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
                            outputStream.flush();
                            return;
                        }
                        bodyIndex = getBodyIndex(headerAccumulator.toByteArray(), headerAccumulator.size());
                    }
                    final var requestBytes = headerAccumulator.toByteArray();
                    final var headerBytes = new ByteArrayInputStream(requestBytes, 0, bodyIndex);
                    final var headerReader = new BufferedReader(new InputStreamReader(headerBytes, StandardCharsets.US_ASCII));
                    headerReader.readLine(); // <METHOD> / HTTP/1.1
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

                    // Body and content-length are relative to the request method - some methods might not contain it.
                    var contentLength = 0;
                    try {
                        if (headers.containsKey("content-length")) {
                            contentLength = Integer.parseInt(headers.get("content-length"));
                        }
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Invalid content-length");
                    }

                    if (contentLength < 0) {
                        outputStream.write("HTTP/1.1 400 Bad Request\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
                        outputStream.flush();
                        // Return might end server?
                        return;
                    }

                    if (!headers.containsKey("host")) {
                        outputStream.write("HTTP/1.1 400 Bad Request\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
                        outputStream.flush();
                        return;
                    }

                    if (headers.containsKey("transfer-encoding")) {
                        outputStream.write("HTTP/1.1 400 Bad Request\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
                        outputStream.flush();
                        return;
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
                            outputStream.write("HTTP/1.1 400 Bad Request\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
                            outputStream.flush();
                            return;
                        }
                        bodyAccumulator.write(buffer, 0, n);

                        if (bodyAccumulator.size() > MAX_BODY_SIZE) {
                            outputStream.write("HTTP/1.1 413 Payload Too Large\r\nContent-Length: 0\r\nContent-Type: text/plain\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
                            outputStream.flush();
                            return;
                        }
                        remaining -= n;
                    }
                    System.out.println("[mini-http] Starting reading body in position [" + bodyIndex + "]");
                    // TODO: Finish response utils to improve response building
                    final var formatter = ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH).withZone(ZoneOffset.UTC);
                    final var response = ResponseUtils.getResponse(200, "Content-Length: 0", "Connection: close", "Date: %s".formatted(formatter.format(Instant.now())), "Content-Type: text/plain", "\r\n");
                    outputStream.write(response.getBytes(StandardCharsets.US_ASCII));
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

    public static class ResponseUtils {
        private static final Map<Integer, String> RESPONSE_STATUS_MAP = Map.of(
                200, "OK",
                400, "Bad Request",
                413, "Payload Too Large"
        );

        public static String getResponse(int status, String... headers) {
            final var joiner = new StringJoiner("\r\n");
            joiner.add("HTTP/1.1 %d %s".formatted(status, RESPONSE_STATUS_MAP.get(status)));
            for (final var header : headers) {
                joiner.add(header);
            }
            return joiner.toString();
        }
    }
}