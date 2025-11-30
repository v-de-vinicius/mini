package com.github.vdevinicius.mini.http.core;

import java.util.HashMap;
import java.util.Map;

public enum HttpResponseStatus {
    // 1XX
    CONTINUE(100, "Continue"),

    // 2XX
    OK(200, "OK"),
    NO_CONTENT(204, "No Content"),

    // 4XX
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),

    // 5XX
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented");

    private static final Map<Integer, HttpResponseStatus> INTEGER_STATUS_MAP = new HashMap<>(values().length);

    static {
        for (final var status : values()) {
            INTEGER_STATUS_MAP.put(status.getIntStatus(), status);
        }
    }

    private final int status;
    private final String simpleDescription;

    HttpResponseStatus(int status, String simpleDescription) {
        this.status = status;
        this.simpleDescription = simpleDescription;
    }

    public int getIntStatus() {
        return this.status;
    }

    public String getSimpleDescription() {
        return this.simpleDescription;
    }

    public static HttpResponseStatus fromInt(int value) {
        return INTEGER_STATUS_MAP.get(value);
    }
}
