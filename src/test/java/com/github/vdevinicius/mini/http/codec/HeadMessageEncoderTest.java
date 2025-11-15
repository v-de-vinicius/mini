package com.github.vdevinicius.mini.http.codec;

import com.github.vdevinicius.mini.http.core.HttpRequest;
import com.github.vdevinicius.mini.http.core.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;

public class HeadMessageEncoderTest {

    private static final Instant INSTANT = Instant.now();
    private static final Clock CLOCK = Clock.fixed(INSTANT, ZoneId.of("America/Sao_Paulo"));
    private static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH).withZone(ZoneOffset.UTC);

    private HeadMessageEncoder sut;

    private HttpRequest.Builder requestBuilder;
    private HttpResponse.Builder responseBuilder;

    @BeforeEach
    public void setUp() {
        requestBuilder = HttpRequest.newBuilder()
                .uri("/api/v1")
                .method("GET")
                .version("1.1")
                .headers(Map.of("Content-Type", "application/json"));
        responseBuilder = HttpResponse.newBuilder()
                .status(200);

        sut = new HeadMessageEncoder(CLOCK);
    }

    @Test
    void shouldEncodeHeadMessageIntoAnArrayOfBytes() {
        final var expected = "HTTP/1.1 200 OK\r\nConnection: Close\r\nContent-Length: 0\r\nDate: %s\r\nContent-Type: text/plain\r\n\r\n".formatted(DATE_TIME_FORMATTER.format(INSTANT)).getBytes(StandardCharsets.UTF_8);
        final var actual = sut.encode(requestBuilder.build(), responseBuilder.build());
        assertThat(actual).isEqualTo(expected);
    }
}
