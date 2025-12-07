package com.github.vdevinicius.mini.http.router;

import com.github.vdevinicius.mini.http.core.MiniHttpRequest;
import com.github.vdevinicius.mini.http.exception.NoHandlerFoundException;

public interface MatchingRouter<T> extends Router<T> {
    MatchedRoute match(MiniHttpRequest req) throws NoHandlerFoundException;
}
