package com.fedex.versionone;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalDate;

public class StoryParser {
    JsonNode root;
    LocalDate defaultStartDate = LocalDate.parse("3000-01-01");
    LocalDate defaultEndDate = LocalDate.parse("1000-01-01");
    public StoryParser(JsonNode root) throws IOException {
        this.root = root;
    }

    public String findStartDate() {
        LocalDate startDate = defaultStartDate;
        for (JsonNode node : root) {
            JsonNode body = node.get("body");
            JsonNode targets = body.get("target");
            if (targets == null) {
                continue;
            }
            for (JsonNode target : targets) {
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
        }

        return startDate == defaultStartDate ? "" : startDate.toString();
    }

    public String findEndDate() {
        LocalDate endDate = defaultEndDate;
        for (JsonNode node : root) {
            JsonNode body = node.get("body");
            JsonNode targets = body.get("target");
            if (targets == null) {
                continue;
            }
            for (JsonNode target : targets) {
                JsonNode newValue = target.get("newValue");
                if (newValue != null && newValue.isTextual()) {
                    if (newValue.asText().equals("Done")) {
                        String date = body.get("time").asText().substring(0, 10);
                        LocalDate potentialStartDate = LocalDate.parse(date);

                        if (potentialStartDate.isAfter(endDate)) {
                            endDate = potentialStartDate;
                        }
                    }
                }
            }
        }

        return endDate == defaultEndDate ? "" : endDate.toString();
    }
}
