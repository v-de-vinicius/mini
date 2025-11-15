package com.github.vdevinicius.mini.http.codec;

import com.github.vdevinicius.mini.http.core.HttpRequest;
import com.github.vdevinicius.mini.http.core.HttpResponse;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;

public abstract class AbstractHttpMessageEncoder implements HttpMessageEncoder {
    protected static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH).withZone(ZoneOffset.UTC);
    protected static final Map<Integer, String> STATUS_DESCRIPTION_MAP = Map.of(
            100, "Continue",
            200, "OK",
            400, "Bad Request",
            404, "Not Found",
            413, "Payload Too Large",
            431, "Request Header Fields Too Large",
            501, "Not Implemented"
    );

    protected final Clock clock;

    protected AbstractHttpMessageEncoder(Clock clock) {
        this.clock = clock;
    }
}
