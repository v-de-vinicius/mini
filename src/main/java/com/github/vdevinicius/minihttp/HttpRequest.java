package com.github.vdevinicius.minihttp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record HttpRequest(HttpMethod method,
                          String uri,
                          HttpVersion version,
                          Map<String, String> headers,
                          byte[] body) {

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HttpRequest that = (HttpRequest) o;
        return Objects.equals(uri, that.uri) && Arrays.equals(body, that.body) && method == that.method && version == that.version && Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, uri, version, headers, Arrays.hashCode(body));
    }

    public static class Builder {
        private HttpMethod method;
        private String uri;
        private HttpVersion version;
        private Map<String, String> headers = new HashMap<>();
        private byte[] body;

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder version(String version) {
            this.version = HttpVersion.valueOf("VERSION_" + version.replace(".", "_"));
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

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this.method, this.uri, this.version, this.headers, this.body);
        }
    }
}
