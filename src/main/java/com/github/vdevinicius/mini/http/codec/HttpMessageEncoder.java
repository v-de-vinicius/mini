package com.github.vdevinicius.mini.http.codec;

import com.github.vdevinicius.mini.http.core.MiniHttpRequest;
import com.github.vdevinicius.mini.http.core.MiniHttpResponse;

public interface HttpMessageEncoder {
    byte[] encode(MiniHttpRequest req, MiniHttpResponse res);
}
