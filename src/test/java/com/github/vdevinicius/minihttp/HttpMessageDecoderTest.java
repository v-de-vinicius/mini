package com.github.vdevinicius.minihttp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpMessageDecoderTest {
    private HttpMessageDecoder sut;

    @BeforeEach
    public void setUp() {
        final var rawHeaders = "HTTP/1.1 POST\r\nHost: localhost\r\nContent-Length: 0\r\n\r\n";
        sut = new HttpMessageDecoder(new ByteArrayInputStream(rawHeaders.getBytes(StandardCharsets.UTF_8)), 1024);
    }

    @Test
    void test() throws Throwable {
        assertThat(sut.getHeaders())
                .contains(Map.entry("host", "localhost"))
                .contains(Map.entry("content-length", "0"));
    }
}
