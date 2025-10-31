package com.github.vdevinicius.minihttp.routing;

import com.github.vdevinicius.minihttp.Handler;
import com.github.vdevinicius.minihttp.HttpRequest;
import com.github.vdevinicius.minihttp.NoHandlerFoundException;

public interface RouteMatcher {
    Handler match(HttpRequest req) throws NoHandlerFoundException;
}
