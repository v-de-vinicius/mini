package com.github.vdevinicius.mini.http;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        new Mini()
                .port(8080)
                .head("/ping", (req, res) -> {
                    res.setStatus(200);
                    res.setBody("{ \"status\": \"UP\" }");
                    res.getHeaders().put("Content-Type", "application/json");
                })
                .start();
    }
}
