package com.github.vdevinicius.mini.http.core;

public interface HttpResponse {
    HttpResponse body(Object object);
    HttpResponse status(int status);
    String getHeader(String key);
    HttpResponse setHeader(String key, String value);
}
