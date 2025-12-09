package com.github.vdevinicius.mini.http.codec;

import com.github.vdevinicius.mini.http.core.HttpMethod;
import com.github.vdevinicius.mini.http.core.HttpRequest;
import com.github.vdevinicius.mini.http.core.HttpResponse;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

public final class HeadMessageEncoder implements HttpMessageEncoder {
    private static final HttpMessageEncoder BODY_MESSAGE_ENCODER = new BodyMessageEncoder(new Gson());
    private final Clock clock;

    public HeadMessageEncoder(Clock clock) {
        this.clock = clock;
    }

    public byte[] encode(HttpRequest req, HttpResponse res) {
        res.setHeader("Date", DateTimeFormatter.RFC_1123_DATE_TIME.format(OffsetDateTime.now(clock)));
        res.setHeader("Connection", "Close");
        final var bodyBytes = BODY_MESSAGE_ENCODER.encode(req, res);
        if (!HttpMethod.HEAD.equals(req.method())) {
            res.setHeader("Content-Length", String.valueOf(bodyBytes.length));
        }
        final var joiner = new StringJoiner("\r\n");
        joiner.add("HTTP/1.1 %d %s".formatted(res.getStatus().getIntStatus(), res.getStatus().getSimpleDescription()));
        res.headers().forEach((key, value) -> joiner.add("%s: %s".formatted(key, value)));
        joiner.add("\r\n");
        final var headBytes = joiner.toString().getBytes(StandardCharsets.UTF_8);
        if (HttpMethod.HEAD.equals(req.method())) {
            return headBytes;
        }
        final var buf = ByteBuffer.allocate(headBytes.length + bodyBytes.length);
        buf.put(headBytes);
        buf.put(bodyBytes);
        return buf.array();
    }
}
