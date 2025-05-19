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

        for (String state : states) {
            stateDates.put(state, null);
        }
    }

    public Map<String, LocalDate> findHistory(String storyId) {
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

        return stateDates;
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
