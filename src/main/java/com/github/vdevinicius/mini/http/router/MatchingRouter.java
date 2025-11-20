package com.github.vdevinicius.mini.http.router;

import com.github.vdevinicius.mini.http.core.HttpRequest;
import com.github.vdevinicius.mini.http.exception.NoHandlerFoundException;

public interface MatchingRouter<T> extends Router<T> {
    MatchedRoute match(HttpRequest req) throws NoHandlerFoundException;
}
