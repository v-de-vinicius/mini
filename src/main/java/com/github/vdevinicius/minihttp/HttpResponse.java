package com.github.vdevinicius.minihttp;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import static java.time.format.DateTimeFormatter.ofPattern;

public record HttpResponse(int status, Map<String, String> headers, String body) {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH).withZone(ZoneOffset.UTC);
    private static final Map<Integer, String> STATUS_DESCRIPTION_MAP = Map.of(
            200, "OK",
            400, "Bad Request",
            413, "Payload Too Large",
            431, "Request Header Fields Too Large",
            501, "Not Implemented"
    );

    public static class Builder {
        private int status;
        private Map<String, String> headers;
        private String body;

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            if (headers == null) throw new IllegalArgumentException("Headers cannot be null");
            this.headers = new HashMap<>(headers);
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public HttpResponse build() {
            if (headers == null) {
                this.headers = new HashMap<>();
            }

            if (body == null) {
                this.headers.put("Content-Length", "0");
            } else {
                this.headers.put("Content-Length", String.valueOf(body.getBytes(StandardCharsets.UTF_8).length));
            }

            if (!headers.containsKey("Content-Type")) {
                this.headers.put("Content-Type", "text/plain");
            }

            return new HttpResponse(this.status, this.headers, this.body);
        }
    }

    // This should be an encoder class
    public byte[] getBytes() {
        this.headers.put("Date", DATE_TIME_FORMATTER.format(Instant.now()));
        this.headers.put("Connection", "Close");
        final var joiner = new StringJoiner("\r\n");
        final var headerJoiner = new StringJoiner("\r\n");
        headerJoiner.add("HTTP/1.1 %d %s".formatted(status, STATUS_DESCRIPTION_MAP.get(status)));
        headers.forEach((key, value) -> headerJoiner.add("%s: %s".formatted(key, value)));
        headerJoiner.add("");
        joiner.add(headerJoiner.toString());
        if (body != null) {
            joiner.add(body);
        }
        return joiner.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static HttpResponse.Builder newBuilder() {
        return new HttpResponse.Builder();
    }
}
