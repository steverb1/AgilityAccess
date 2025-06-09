package com.access.versionone;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Map;

public class ApiServer {
    private final int port;

    public ApiServer(int port) {
        this.port = port;
    }

    public void start() {
        Javalin app = Javalin.create()
            .get("/health", ctx -> ctx.result("Server is running"))
            .post("/api/stories/extract", this::extractStoryActivity)
            .start(port);
    }

    private void extractStoryActivity(Context ctx) {
        try {
            Map<String, String> requestParams = ctx.bodyAsClass(Map.class);
            String csvContent = V1Accessor.extractStoryActivity(requestParams);

            ctx.contentType("text/csv")
               .header("Content-Disposition", "attachment; filename=stories.csv")
               .result(csvContent);

        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getProperty("port", "7070"));
        new ApiServer(port).start();
    }
}
