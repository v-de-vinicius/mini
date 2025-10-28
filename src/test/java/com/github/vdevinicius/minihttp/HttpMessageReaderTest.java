package com.github.vdevinicius.minihttp;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpMessageReaderTest {

    private static final int BUFFER_SIZE = 1024;

    @Test
    void shouldReturnAnArrayOfBytes() throws IOException {
        final var rawBody = "{ \"id\": 123 }";
        final var bodyBytes = rawBody.getBytes(StandardCharsets.UTF_8).length;
        final var rawMessage = "GET / HTTP/1.1\r\nContent-Length: %d\r\nHost: 127.0.0.1\r\n\r\n%s".formatted(bodyBytes, rawBody);
        final var in = new ByteArrayInputStream(rawMessage.getBytes(StandardCharsets.UTF_8));
        final var sut = new HttpMessageReader(in, 1024);
        final var result = sut.read();
        assertThat(rawMessage.getBytes(StandardCharsets.UTF_8)).isEqualTo(result);
    }

    @Test
    void whenHeaderContainsEOFBeforeEnd_thenShouldThrowIOException() {
        final var rawMessage = "GET / HTTP/1.1\r\nContent-Length:";
        final var in = new ByteArrayInputStream(rawMessage.getBytes(StandardCharsets.UTF_8));
        final var sut = new HttpMessageReader(in, 1024);
        assertThrows(IOException.class, sut::read);
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
        final var sut = new HttpMessageReader(in, BUFFER_SIZE);
        assertThrows(InvalidHttpMessageException.class, sut::read);
    }

    @Test
    void whenBodySizeExceedTheMaximumPermitted_thenShouldThrowInvalidHttpMessageException() {
        final var joiner = new StringJoiner("\r\n");
        joiner.add("GET / HTTP/1.1");
        joiner.add("Host: 127.0.0.1");
        joiner.add("");
        for (var i = 0; i < 33000; i++) {
            joiner.add("Mini-Body-%1$d: %1$d".formatted(i));
        }
        joiner.add("");
        final var rawMessage = joiner.toString();
        final var in = new ByteArrayInputStream(rawMessage.getBytes(StandardCharsets.UTF_8));
        final var sut = new HttpMessageReader(in, BUFFER_SIZE);
        assertThrows(InvalidHttpMessageException.class, sut::read);
    }
}
