package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class StoryParser {
    JsonNode root;
    List<String> states;
    Map<String, LocalDate> stateDates = new LinkedHashMap<>();

    public StoryParser(JsonNode root) throws IOException {
        this.root = root;
        String configuredStates = PropertyFetcher.getProperty("states");
        states = Arrays.stream(configuredStates.split(","))
                .map(String::trim)
                .toList();
    }

    public StoryHistory findHistory(String storyId) {
        for (JsonNode node : root) {
            JsonNode body = node.get("body");
            JsonNode targets = body.get("target");
            if (targets == null) {
                continue;
            }
            for (JsonNode target : targets) {
                JsonNode newValue = target.get("newValue");
                if (newValue != null && newValue.isTextual()) {
                    if (states.contains(newValue.asText())) {
                        String date = body.get("time").asText().substring(0, 10);
                        update(newValue.asText(), LocalDate.parse(date));
                    }
                }
            }
        }

        String readyForBuildDate = stateDates.get("Ready for Build") == null ? "" : stateDates.get("Ready for Build").toString();
        String buildDate = stateDates.get("Build") == null ? "" : stateDates.get("Build").toString();
        String doneDate = stateDates.get("Done") == null ? "" : stateDates.get("Done").toString();

        return new StoryHistory(storyId.substring(6), readyForBuildDate, buildDate, doneDate);
    }

    void update(String state, LocalDate date) {
        boolean finalState = isLastState(state);

        if (stateDates.get(state) == null) {
            stateDates.put(state, date);
        }
        else {
            if (!finalState) {
                if (date.isBefore(stateDates.get(state))) {
                    stateDates.put(state, date);
                }
            }
            else {
                if (date.isAfter(stateDates.get(state))) {
                    stateDates.put(state, date);
                }
            }
        }
    }

    boolean isLastState(String state) {
        return state.equals(states.getLast());
    }
}
