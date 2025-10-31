package com.github.vdevinicius.mini.http.router;

import com.github.vdevinicius.mini.http.core.Handler;

public interface Router<T> {
    T get(String path, Handler handler);
    T post(String path, Handler handler);
    T put(String path, Handler handler);
    T patch(String path, Handler handler);
    T delete(String path, Handler handler);
    T head(String path, Handler handler);
    T connect(String path, Handler handler);
    T options(String path, Handler handler);
    T trace(String path, Handler handler);
}
