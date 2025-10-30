package com.github.vdevinicius.minihttp;

import java.util.Map;

public interface HttpRequest {
    HttpMethod method();
    Map<String, String> headers();
}
