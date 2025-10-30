package com.github.vdevinicius.minihttp.routing;

import com.github.vdevinicius.minihttp.HttpMethod;
import com.github.vdevinicius.minihttp.HttpRequest;
import com.github.vdevinicius.minihttp.HttpResponse;
import com.github.vdevinicius.minihttp.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class SimpleRouter implements Router, RouteMatcher {
    private final Map<RouteKey, BiConsumer<HttpRequest, HttpResponse>> routeMap = new HashMap<>();

    @Override
    public Router get(String path, BiConsumer<HttpRequest, HttpResponse> handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.GET, path, handler);
        return this;
    }

    @Override
    public Router post(String path, BiConsumer<HttpRequest, HttpResponse> handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.POST, path, handler);
        return this;
    }

    @Override
    public Router put(String path, BiConsumer<HttpRequest, HttpResponse> handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.PUT, path, handler);
        return this;
    }

    @Override
    public Router patch(String path, BiConsumer<HttpRequest, HttpResponse> handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.PATCH, path, handler);
        return this;
    }

    @Override
    public Router delete(String path, BiConsumer<HttpRequest, HttpResponse> handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.DELETE, path, handler);
        return this;
    }

    @Override
    public Router head(String path, BiConsumer<HttpRequest, HttpResponse> handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.HEAD, path, handler);
        return this;
    }

    @Override
    public Router connect(String path, BiConsumer<HttpRequest, HttpResponse> handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.CONNECT, path, handler);
        return this;
    }

    @Override
    public Router options(String path, BiConsumer<HttpRequest, HttpResponse> handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.OPTIONS, path, handler);
        return this;
    }

    @Override
    public Router trace(String path, BiConsumer<HttpRequest, HttpResponse> handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.TRACE, path, handler);
        return this;
    }

    private void addRouteIfNotExistsOrThrowException(HttpMethod method, String path, BiConsumer<HttpRequest, HttpResponse> handler) {
        final var key = new RouteKey(method, path);
        if (this.routeMap.containsKey(key)) {
            throw new UnsupportedOperationException("Cannot redeclare an already declared route");
        }
        this.routeMap.put(key, handler);
    }

    @Override
    public BiConsumer<HttpRequest, HttpResponse> match(HttpRequest req) throws NoHandlerFoundException {
        final var key = new RouteKey(req.method(), req.uri());
        var handler = tryMatchStatically(key);
        if (handler != null) return handler;
        handler = tryMatchDynamically(key);
        if (handler != null) return handler;
        throw new NoHandlerFoundException(req.method(), req.uri());
    }

    private BiConsumer<HttpRequest, HttpResponse> tryMatchStatically(RouteKey key) {
        return this.routeMap.get(key);
    }

    private BiConsumer<HttpRequest, HttpResponse> tryMatchDynamically(RouteKey key) {
        final var pathFragments = key.uri().split("/");
        return this.routeMap.entrySet().stream()
                .filter(it -> key.method().equals(it.getKey().method()))
                .filter(it -> {
                    final var routePathFragments = it.getKey().uri().split("/");
                    if (pathFragments.length != routePathFragments.length) {
                        return false;
                    }
                    for (var i = 0; i < pathFragments.length; i++) {
                        if (!pathFragments[i].equals(routePathFragments[i]) && !pathFragments[i].startsWith("{")) {
                            return false;
                        }
                    }
                    return true;
                })
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }
}
