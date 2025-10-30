package com.github.vdevinicius.minihttp.routing;

import com.github.vdevinicius.minihttp.HttpRequest;
import com.github.vdevinicius.minihttp.HttpResponse;

import java.util.function.BiConsumer;

public interface Router {
    Router get(String path, BiConsumer<HttpRequest, HttpResponse> handler);
    Router post(String path, BiConsumer<HttpRequest, HttpResponse> handler);
    Router put(String path, BiConsumer<HttpRequest, HttpResponse> handler);
    Router patch(String path, BiConsumer<HttpRequest, HttpResponse> handler);
    Router delete(String path, BiConsumer<HttpRequest, HttpResponse> handler);
    Router head(String path, BiConsumer<HttpRequest, HttpResponse> handler);
    Router connect(String path, BiConsumer<HttpRequest, HttpResponse> handler);
    Router options(String path, BiConsumer<HttpRequest, HttpResponse> handler);
    Router trace(String path, BiConsumer<HttpRequest, HttpResponse> handler);
}
