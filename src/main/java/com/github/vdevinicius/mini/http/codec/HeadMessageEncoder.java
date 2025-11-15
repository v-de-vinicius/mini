package com.github.vdevinicius.mini.http.codec;

import com.github.vdevinicius.mini.http.core.HttpRequest;
import com.github.vdevinicius.mini.http.core.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.StringJoiner;

public class HeadMessageEncoder extends AbstractHttpMessageEncoder {

    public HeadMessageEncoder(Clock clock) {
        super(clock);
    }

    public byte[] encode(HttpRequest req, HttpResponse res) {
        res.getHeaders().put("Date", DATE_TIME_FORMATTER.format(Instant.now(clock)));
        res.getHeaders().put("Connection", "Close");
        final var joiner = new StringJoiner("\r\n");
        joiner.add("HTTP/1.1 %d %s".formatted(res.getStatus(), STATUS_DESCRIPTION_MAP.get(res.getStatus())));
        res.getHeaders().forEach((key, value) -> joiner.add("%s: %s".formatted(key, value)));
        joiner.add("\r\n");
        return joiner.toString().getBytes(StandardCharsets.UTF_8);
    }
}
