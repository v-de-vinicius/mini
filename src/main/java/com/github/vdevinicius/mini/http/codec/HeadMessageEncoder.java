package com.github.vdevinicius.mini.http.codec;

import com.github.vdevinicius.mini.http.core.MiniHttpRequest;
import com.github.vdevinicius.mini.http.core.MiniHttpResponse;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

public class HeadMessageEncoder implements HttpMessageEncoder {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

    private final Clock clock;

    public HeadMessageEncoder(Clock clock) {
        this.clock = clock;
    }

    public byte[] encode(MiniHttpRequest req, MiniHttpResponse res) {
        res.setHeader("Date", DATE_TIME_FORMATTER.format(Instant.now(clock)));
        res.setHeader("Connection", "Close");
        final var joiner = new StringJoiner("\r\n");
        joiner.add("HTTP/1.1 %d %s".formatted(res.getStatus().getIntStatus(), res.getStatus().getSimpleDescription()));
        res.getHeaders().forEach((key, value) -> joiner.add("%s: %s".formatted(key, value)));
        joiner.add("\r\n");
        return joiner.toString().getBytes(StandardCharsets.UTF_8);
    }
}
