package com.github.vdevinicius.mini.http;

import com.google.gson.annotations.SerializedName;

public class Main {
    public static void main(String[] args) {
        Mini.newServer()
                .port(8080)
                .head("/ping", (req, res) -> res.status(204))
                .post("/graphql", (req, res) -> res.body(req.body()))
                .get("/produce/{id}", (req, res) -> {
                    final var body = new MyObject();
                    body.a = "bar";
                    body.b = "foo";
                    res
                            .setHeader("Content-Type", "application/json")
                            .status(200)
                            .body(body);
                })
                .start();
    }

    public static class MyObject {
        @SerializedName("foo")
        public String a;
        @SerializedName("bar")
        public String b;
    }
}
