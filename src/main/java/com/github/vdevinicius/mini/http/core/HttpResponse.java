package com.github.vdevinicius.mini.http.core;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import static java.time.format.DateTimeFormatter.ofPattern;

// TODO: This class should be mostly an interface with an underlying implementation which can manipulate response reading/sending.
public final class HttpResponse {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH).withZone(ZoneOffset.UTC);

    public HttpResponse(int status, String body, Map<String, String> headers) {
        this.responseStatus = HttpResponseStatus.fromInt(status);
        this.body = body;
        this.headers = headers;
    }

    public void writeStatus(HttpResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public void writeStatus(int responseStatus) {
        this.responseStatus = HttpResponseStatus.fromInt(responseStatus);
    }

    public String getBody() {
        return body;
    }

    public void writeBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public HttpResponseStatus getResponseStatus() {
        return responseStatus;
    }

    private HttpResponseStatus responseStatus;
    private String body;
    private Map<String, String> headers;

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

            return new HttpResponse(this.status, this.body, this.headers);
        }
    }

    // This should be an encoder class
    public byte[] getBytes() {
        this.headers.put("Date", DATE_TIME_FORMATTER.format(Instant.now()));
        this.headers.put("Connection", "Close");
        this.headers.put("Content-Length", String.valueOf(this.body.getBytes(StandardCharsets.UTF_8).length));
        final var joiner = new StringJoiner("\r\n");
        final var headerJoiner = new StringJoiner("\r\n");
        headerJoiner.add("HTTP/1.1 %d %s".formatted(responseStatus.getIntStatus(), responseStatus.getSimpleDescription()));
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
