package com.github.vdevinicius.mini.http.core;

import java.util.Map;

public interface HttpRequest {
    String path();
    String matchedByPath();
    HttpMethod method();
    HttpVersion version();
    Map<String, String> headers();
    Map<String, String> queryParams();
    Map<String, String> pathVariables();
    byte[] body();
}
