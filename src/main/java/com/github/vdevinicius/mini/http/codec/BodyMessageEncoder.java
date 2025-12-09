package com.github.vdevinicius.mini.http.codec;

import com.github.vdevinicius.mini.http.core.HttpRequest;
import com.github.vdevinicius.mini.http.core.HttpResponse;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

public final class BodyMessageEncoder implements HttpMessageEncoder {

    private final Gson gson;

    public BodyMessageEncoder(Gson gson) {
        this.gson = gson;
    }

    @Override
    public byte[] encode(HttpRequest req, HttpResponse res) {
        try {
            return gson.toJson(res.getBody()).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
