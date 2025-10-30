package com.github.vdevinicius.minihttp.routing;

import com.github.vdevinicius.minihttp.HttpRequest;
import com.github.vdevinicius.minihttp.HttpResponse;
import com.github.vdevinicius.minihttp.NoHandlerFoundException;

import java.util.function.BiConsumer;

public interface RouteMatcher {
    BiConsumer<HttpRequest, HttpResponse> match(HttpRequest req) throws NoHandlerFoundException;
}
