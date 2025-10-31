package com.github.vdevinicius.mini.http.router;

import com.github.vdevinicius.mini.http.core.Handler;

public record MatchedRoute(String matchedUri, Handler handler) {
    public static MatchedRoute of(String matchedUri, Handler handler) {
        return new MatchedRoute(matchedUri, handler);
    }
}
