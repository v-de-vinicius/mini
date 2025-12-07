package com.github.vdevinicius.mini.http.core;

import com.google.gson.GsonBuilder;

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
public final class MiniHttpResponse implements HttpResponse {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH).withZone(ZoneOffset.UTC);

    public MiniHttpResponse(int status, Object body, Map<String, String> headers) {
        this.responseStatus = HttpResponseStatus.fromInt(status);
        this.body = body;
        this.headers = headers;
    }

    private Object body;
    private HttpResponseStatus responseStatus;
    private Map<String, String> headers;

    @Override
    public HttpResponse body(Object body) {
        this.body = body;
        return this;
    }

    public HttpResponseStatus getStatus() {
        return this.responseStatus;
    }

    @Override
    public HttpResponse status(int status) {
        this.responseStatus = HttpResponseStatus.fromInt(status);
        return this;
    }

    @Override
    public String getHeader(String key) {
        return this.headers.get(key);
    }

    @Override
    public HttpResponse setHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public static class Builder {
        private int status;
        private Map<String, String> headers;
        private String body;

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public MiniHttpResponse build() {
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

            return new MiniHttpResponse(this.status, this.body, this.headers);
        }
    }

    // This should be an encoder class
    public byte[] getBytes() {
        final var body = new GsonBuilder().create().toJson(this.body);
        final var bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        this.headers.put("Date", DATE_TIME_FORMATTER.format(Instant.now()));
        this.headers.put("Connection", "Close");
        this.headers.put("Content-Length", String.valueOf(bodyBytes.length));
        final var joiner = new StringJoiner("\r\n");
        final var headerJoiner = new StringJoiner("\r\n");
        headerJoiner.add("HTTP/1.1 %d %s".formatted(responseStatus.getIntStatus(), responseStatus.getSimpleDescription()));
        headers.forEach((key, value) -> headerJoiner.add("%s: %s".formatted(key, value)));
        headerJoiner.add("");
        joiner.add(headerJoiner.toString());
        if (this.body != null) {
            joiner.add(body);
        }
        return joiner.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static MiniHttpResponse.Builder newBuilder() {
        return new MiniHttpResponse.Builder();
    }
}
