package com.github.vdevinicius.mini.http.codec;

import com.github.vdevinicius.mini.http.core.HttpRequest;
import com.github.vdevinicius.mini.http.core.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.StringJoiner;

import static java.time.format.DateTimeFormatter.ofPattern;

public class HeadMessageEncoder implements HttpMessageEncoder {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH).withZone(ZoneOffset.UTC);

    private final Clock clock;

    public HeadMessageEncoder(Clock clock) {
        this.clock = clock;
    }

    public byte[] encode(HttpRequest req, HttpResponse res) {
        res.getHeaders().put("Date", DATE_TIME_FORMATTER.format(Instant.now(clock)));
        res.getHeaders().put("Connection", "Close");
        final var joiner = new StringJoiner("\r\n");
        final var status = res.getResponseStatus();
        joiner.add("HTTP/1.1 %d %s".formatted(status.getIntStatus(), status.getSimpleDescription()));
        res.getHeaders().forEach((key, value) -> joiner.add("%s: %s".formatted(key, value)));
        joiner.add("\r\n");
        return joiner.toString().getBytes(StandardCharsets.UTF_8);
    }
}
