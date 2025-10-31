package com.github.vdevinicius.minihttp.routing;

import com.github.vdevinicius.minihttp.*;

import java.util.HashMap;
import java.util.Map;

public class SimpleRouter implements Router<SimpleRouter>, RouteMatcher {
    private final Map<RouteKey, Handler> routeMap = new HashMap<>();

    @Override
    public SimpleRouter get(String path, Handler handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.GET, path, handler);
        return this;
    }

    @Override
    public SimpleRouter post(String path, Handler handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.POST, path, handler);
        return this;
    }

    @Override
    public SimpleRouter put(String path, Handler handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.PUT, path, handler);
        return this;
    }

    @Override
    public SimpleRouter patch(String path, Handler handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.PATCH, path, handler);
        return this;
    }

    @Override
    public SimpleRouter delete(String path, Handler handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.DELETE, path, handler);
        return this;
    }

    @Override
    public SimpleRouter head(String path, Handler handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.HEAD, path, handler);
        return this;
    }

    @Override
    public SimpleRouter connect(String path, Handler handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.CONNECT, path, handler);
        return this;
    }

    @Override
    public SimpleRouter options(String path, Handler handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.OPTIONS, path, handler);
        return this;
    }

    @Override
    public SimpleRouter trace(String path, Handler handler) {
        addRouteIfNotExistsOrThrowException(HttpMethod.TRACE, path, handler);
        return this;
    }

    private void addRouteIfNotExistsOrThrowException(HttpMethod method, String path, Handler handler) {
        final var key = new RouteKey(method, path);
        if (this.routeMap.containsKey(key)) {
            throw new UnsupportedOperationException("Cannot redeclare an already declared route");
        }
        this.routeMap.put(key, handler);
    }

    @Override
    public Handler match(HttpRequest req) throws NoHandlerFoundException {
        // TODO: Normalize request URI (remove query params and fragments)
        final var key = new RouteKey(req.method(), req.uri());
        var handler = tryMatchStatically(key);
        if (handler != null) return handler;
        handler = tryMatchDynamically(key);
        if (handler != null) return handler;
        throw new NoHandlerFoundException(req.method(), req.uri());
    }

    private Handler tryMatchStatically(RouteKey key) {
        return this.routeMap.get(key);
    }

    private Handler tryMatchDynamically(RouteKey key) {
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
