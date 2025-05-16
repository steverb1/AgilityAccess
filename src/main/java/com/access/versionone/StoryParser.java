package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class StoryParser {
    JsonNode root;
    List<String> states = new ArrayList<>() {{
        add("Ready for Build");
        add("Build");
        add("Done");
    }};
    StateChanges stateChanges = new StateChanges();

    public StoryParser(JsonNode root) throws IOException {
        this.root = root;
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

    private static class StateChanges {
        LocalDate readyForBuild;
        LocalDate build;
        LocalDate done;

        void update(String state, LocalDate date) {
            if (state.equals("Ready for Build")) {
                if (readyForBuild == null) {
                    readyForBuild = date;
                }
                else {
                    if (date.isBefore(readyForBuild)) {
                        readyForBuild = date;
                    }
                }
            }
            else if (state.equals("Build")) {
                if (build == null) {
                    build = date;
                }
                else {
                    if (date.isBefore(build)) {
                        build = date;
                    }
                }
            }
            else if (state.equals("Done")) {
                if (done == null) {
                    done = date;
                }
                else {
                    if (date.isAfter(done)) {
                        done = date;
                    }
                }
            }
        }
    }
}
