package com.github.vdevinicius.mini.http.codec;

import com.github.vdevinicius.mini.http.core.HttpRequest;
import com.github.vdevinicius.mini.http.exception.MalformedHttpMessageException;
import com.github.vdevinicius.mini.http.codec.decoder.HttpMessageDecoder;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpMessageDecoderTest {

    private static final int BUFFER_SIZE = 1024;

    @Test
    void shouldReturnDecodedHttpRequest() throws IOException {
        final var rawBody = "{ \"id\": 123 }";
        final var bodyBytes = rawBody.getBytes(StandardCharsets.UTF_8).length;
        final var rawMessage = "GET / HTTP/1.1\r\nContent-Length: %d\r\nHost: 127.0.0.1\r\n\r\n%s".formatted(bodyBytes, rawBody);
        final var in = new ByteArrayInputStream(rawMessage.getBytes(StandardCharsets.UTF_8));
        final var sut = new HttpMessageDecoder(in, BUFFER_SIZE);
        final var result = sut.read();
        assertThat(result).isEqualTo(HttpRequest.newBuilder()
                        .method("GET")
                        .version("1.1")
                        .uri("/")
                        .setHeader("content-length", String.valueOf(bodyBytes))
                        .setHeader("host", "127.0.0.1")
                        .body(rawBody.getBytes(StandardCharsets.UTF_8))
                        .build());
    }

    @Test
    void whenHeaderContainsEOFBeforeEnd_thenShouldThrowIOException() {
        final var rawMessage = "GET / HTTP/1.1\r\nContent-Length:";
        final var in = new ByteArrayInputStream(rawMessage.getBytes(StandardCharsets.UTF_8));
        final var sut = new HttpMessageDecoder(in, BUFFER_SIZE);
        assertThrows(EOFException.class, sut::read);
    }

    @Test
    void whenHeaderSizeExceedsTheMaximumPermitted_thenShouldThrowInvalidHttpMessageException() {
        final var joiner = new StringJoiner("\r\n");
        joiner.add("GET / HTTP/1.1");
        joiner.add("Host: 127.0.0.1");
        for (var i = 0; i < 33000; i++) {
            joiner.add("Mini-Header-%1$d: %1$d".formatted(i));
        }
        joiner.add("");
        final var rawMessage = joiner.toString();
        final var in = new ByteArrayInputStream(rawMessage.getBytes(StandardCharsets.UTF_8));
        final var sut = new HttpMessageDecoder(in, BUFFER_SIZE);
        assertThrows(MalformedHttpMessageException.class, sut::read);
    }

    @Test
    void whenBodySizeExceedTheMaximumPermitted_thenShouldThrowInvalidHttpMessageException() {
        final var joiner = new StringJoiner("\r\n");
        joiner.add("GET / HTTP/1.1");
        joiner.add("Host: 127.0.0.1");
        final var builder = new StringBuilder();
        for (var i = 0; i < 33000; i++) {
            builder.append("Mini-Body-%1$d: %1$d".formatted(i));
        }
        final var body = builder.toString();
        joiner.add("Content-Length: %d".formatted(body.getBytes(StandardCharsets.UTF_8).length));
        joiner.add("");
        joiner.add(body);
        final var rawMessage = joiner.toString();
        final var in = new ByteArrayInputStream(rawMessage.getBytes(StandardCharsets.UTF_8));
        final var sut = new HttpMessageDecoder(in, BUFFER_SIZE);
        assertThrows(MalformedHttpMessageException.class, sut::read);
    }
}
