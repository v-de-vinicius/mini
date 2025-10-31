package com.github.vdevinicius.mini.http.router;

import com.github.vdevinicius.mini.http.core.HttpMethod;

public record RouteKey(HttpMethod method, String uri) {
}
