package com.github.vdevinicius.mini.http;

public class Main {
    public static void main(String[] args) {
        Mini.newServer()
                .port(8080)
                .head("/ping", (req, res) -> res.setStatus(204))
                .start();
    }
}
