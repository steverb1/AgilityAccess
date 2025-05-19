package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class StoryParser {
    JsonNode root;
    List<String> states = new ArrayList<>();
    StateChanges stateChanges = new StateChanges();

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
                        stateChanges.update(newValue.asText(), LocalDate.parse(date));
                    }
                }
            }
        }

        String readyForBuildDate = stateChanges.readyForBuild == null ? "" : stateChanges.readyForBuild.toString();
        String buildDate = stateChanges.build == null ? "" : stateChanges.build.toString();
        String doneDate = stateChanges.done == null ? "" : stateChanges.done.toString();

        return new StoryHistory(storyId.substring(6), readyForBuildDate, buildDate, doneDate);
    }

    private class StateChanges {
        LocalDate readyForBuild;
        LocalDate build;
        LocalDate done;

        Map<String, LocalDate> stateDates = new LinkedHashMap<>();

        StateChanges() {
            for (String state : states) {
                stateDates.put(state, null);
            }
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

            switch (state) {
                case "Ready for Build" -> {
                    if (readyForBuild == null) {
                        readyForBuild = date;
                    } else {
                        if (date.isBefore(readyForBuild)) {
                            readyForBuild = date;
                        }
                    }
                }
                case "Build" -> {
                    if (build == null) {
                        build = date;
                    } else {
                        if (date.isBefore(build)) {
                            build = date;
                        }
                    }
                }
                case "Done" -> {
                    if (done == null) {
                        done = date;
                    } else {
                        if (date.isAfter(done)) {
                            done = date;
                        }
                    }
                }
            }
        }

        boolean isLastState(String state) {
            String lastKey = null;
            for (String currentKey : stateDates.keySet()) {
                lastKey = currentKey;
            }

            return state.equals(lastKey);
        }
    }
}
