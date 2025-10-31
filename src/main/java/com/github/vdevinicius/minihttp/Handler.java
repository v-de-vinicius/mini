package com.github.vdevinicius.minihttp;

@FunctionalInterface
public interface Handler {
    void handle(HttpRequest req, HttpResponse res);
}
