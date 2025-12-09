package com.github.vdevinicius.mini.http.codec;

import com.github.vdevinicius.mini.http.core.HttpRequest;
import com.github.vdevinicius.mini.http.core.HttpResponse;

public interface HttpMessageEncoder {
    byte[] encode(HttpRequest req, HttpResponse res);
}
