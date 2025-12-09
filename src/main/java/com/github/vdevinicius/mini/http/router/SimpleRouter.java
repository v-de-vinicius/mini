package com.github.vdevinicius.mini.http.router;

import com.github.vdevinicius.mini.http.core.Handler;
import com.github.vdevinicius.mini.http.core.HttpMethod;
import com.github.vdevinicius.mini.http.core.HttpRequest;
import com.github.vdevinicius.mini.http.exception.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

public class SimpleRouter implements MatchingRouter<SimpleRouter> {
    private final Map<RouteKey, Handler> routes = new HashMap<>();

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
        if (this.routes.containsKey(key)) {
            throw new UnsupportedOperationException("Cannot redeclare an already declared route");
        }
        this.routes.put(key, handler);
    }

    @Override
    public MatchedRoute match(HttpRequest req) throws NoHandlerFoundException {
        // TODO: Normalize request URI (remove query params and fragments)
        final var key = new RouteKey(req.method(), req.path());
        var matchedRoute = tryMatchStatically(key);
        if (matchedRoute != null) return matchedRoute;
        matchedRoute = tryMatchDynamically(key);
        if (matchedRoute != null) return matchedRoute;
        throw new NoHandlerFoundException(req);
    }

    private MatchedRoute tryMatchStatically(RouteKey key) {
        return ofNullable(this.routes.get(key))
                .map(it -> MatchedRoute.of(key.uri(), it))
                .orElse(null);
    }

    private MatchedRoute tryMatchDynamically(RouteKey key) {
        final var pathFragments = key.uri().split("/");
        return this.routes.entrySet().stream()
                .filter(it -> key.method().equals(it.getKey().method()))
                .filter(it -> {
                    final var routePathFragments = it.getKey().uri().split("/");
                    if (pathFragments.length != routePathFragments.length) {
                        return false;
                    }
                    for (var i = 0; i < pathFragments.length; i++) {
                        if (!pathFragments[i].equals(routePathFragments[i]) && !routePathFragments[i].startsWith("{")) {
                            return false;
                        }
                    }
                    return true;
                })
                .findFirst()
                .map(it -> MatchedRoute.of(it.getKey().uri(), it.getValue()))
                .orElse(null);
    }
}
