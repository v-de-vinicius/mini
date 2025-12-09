package com.github.vdevinicius.mini.http.core;

import java.util.Map;

public interface HttpResponse {
    HttpResponse body(Object object);
    Object getBody();
    HttpResponse status(int status);
    HttpResponseStatus getStatus();
    String getHeader(String key);
    HttpResponse setHeader(String key, String value);
    Map<String, String> headers();
}
