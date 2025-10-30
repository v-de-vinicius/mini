package com.github.vdevinicius.minihttp.routing;

import com.github.vdevinicius.minihttp.HttpMethod;

public record RouteKey(HttpMethod method, String uri) {
}
