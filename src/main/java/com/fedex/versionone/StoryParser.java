package com.fedex.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

public class StoryParser {
    JsonNode root;
    public StoryParser(JsonNode root) throws IOException {
        this.root = root;
    }

    public LocalDate findStartDate() {
        LocalDate startDate = LocalDate.now();
        for (JsonNode node : root) {
            JsonNode body = node.get("body");
            JsonNode target = body.get("target").get(0);
            JsonNode newValue = target.get("newValue");
            if (newValue != null && newValue.isTextual()) {
                if (newValue.asText().equals("Ready for Build")) {
                    String date = body.get("time").asText().substring(0, 10);
                    LocalDate potentialStartDate = LocalDate.parse(date);

                    if (potentialStartDate.isBefore(startDate)) {
                        startDate = potentialStartDate;
                    }
                }
            }
        }
        return startDate;
    }
}
