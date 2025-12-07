package com.github.vdevinicius.mini.http.core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class MiniHttpRequest implements HttpRequest {
    private final String path;
    private final String matchedByPath;
    private final HttpMethod method;
    private final HttpVersion version;
    private final Map<String, String> headers;
    private final byte[] body;

    private MiniHttpRequest(String path, String matchedByPath, HttpMethod method, HttpVersion version, Map<String, String> headers, byte[] body) {
        this.path = path;
        this.matchedByPath = matchedByPath;
        this.method = method;
        this.version = version;
        this.headers = headers;
        this.body = body;
    }

    public static MiniHttpRequest of(MiniHttpRequest req, Consumer<Builder> builderConsumer) {
        final var builder = newBuilder()
                .path(req.path)
                .matchedByRoute(req.matchedByPath)
                .method(req.method)
                .version(req.version)
                .headers(req.headers)
                .body(req.body);
        builderConsumer.accept(builder);
        return builder.build();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String path;
        private String matchedByPath;
        private HttpMethod method;
        private HttpVersion version;
        private Map<String, String> headers;
        private byte[] body;

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder matchedByRoute(String matchedByRoute) {
            this.matchedByPath = matchedByRoute;
            return this;
        }

        public Builder version(String version) {
            this.version = HttpVersion.valueOf("VERSION_" + version.replace(".", "_"));
            return this;
        }

        public Builder version(HttpVersion version) {
            this.version = version;
            return this;
        }

        public Builder setHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder method(String s) {
            this.method = HttpMethod.valueOf(s);
            return this;
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public MiniHttpRequest build() {
            return new MiniHttpRequest(this.path, this.matchedByPath, this.method, this.version, this.headers, this.body);
        }
    }

    @Override
    public String path() {
        return this.path;
    }

    @Override
    public String matchedByPath() {
        return this.matchedByPath;
    }

    @Override
    public HttpMethod method() {
        return this.method;
    }

    @Override
    public HttpVersion version() {
        return this.version;
    }

    @Override
    public Map<String, String> headers() {
        return this.headers;
    }

    @Override
    public Map<String, String> queryParams() {
        return Map.of();
    }

    @Override
    public Map<String, String> pathVariables() {
        return Map.of();
    }

    @Override
    public byte[] body() {
        return this.body;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MiniHttpRequest that = (MiniHttpRequest) o;
        return Objects.equals(path, that.path) && Arrays.equals(body, that.body) && method == that.method && version == that.version && Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path, version, headers, Arrays.hashCode(body));
    }

    public String readBodyAsString() throws IOException {
        final var reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(body)));
        final var builder = new StringBuilder();
        var line = reader.readLine();
        while (line != null && !line.isEmpty()) {
            builder.append(line);
            line = reader.readLine();
        }
        return builder.toString();
    }
}
