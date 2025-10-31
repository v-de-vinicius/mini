package com.github.vdevinicius.mini.http.core;

@FunctionalInterface
public interface Handler {
    void handle(HttpRequest req, HttpResponse res);
}
