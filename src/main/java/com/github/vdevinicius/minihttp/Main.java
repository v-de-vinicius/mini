package com.github.vdevinicius.minihttp;

public class Main {
    public static void main(String[] args) {
        new Mini()
                .port(8080)
                .post("/graphql", (req, res) -> {
                    try {
                        res.setBody(req.readBodyAsString());
                        res.getHeaders().put("Content-Type", "application/json");
                        res.setStatus(200);
                    } catch (Exception e) {
                        res.setBody("unknown exception");
                        res.setStatus(500);
                    }
                })
                .start();
    }
}
