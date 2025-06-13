package com.access.versionone;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class V1Accessor {
    public static void main(String[] args) throws IOException, InterruptedException {
        Map<String, String> properties;

        if (args.length == 7) {
            properties = parseArgs(args);
        }
        else {
            properties = PropertyFetcher.getPropertyMap();
        }
        String csvContent = extractStoryActivity(properties);

        try (FileWriter writer = new FileWriter("stories.csv")) {
            writer.write(csvContent);
        }
    }

    public static String extractStoryActivity(Map<String, String> properties) throws IOException, InterruptedException {
        ActivityFetcher activityFetcher = new ActivityFetcher(new HttpClientWrapper(),
                properties.get("v1.url"), properties.get("v1.token"));

        Map<String, String> teamOidToTeamName = activityFetcher.getTeamsToProcess(
                properties.get("v1.planningLevel"),
                properties.get("v1.team"));

        List<StoryHistory> histories = activityFetcher.getStoryHistories(teamOidToTeamName,
                properties.get("includeStoryPoints").equals("true"),
                properties.get("includeTeamName").equals("true"),
                properties.get("fromClosedDate"),
                properties.get("states"));

        return new OutputGenerator().createCsvContent(histories,
                properties.get("includeStoryPoints").equals("true"),
                properties.get("includeTeamName").equals("true"),
                properties.get("states"));
    }

    static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (String line : args) {
            String trimmed = line.substring(2);
            int eq = trimmed.indexOf('=');
            String key = trimmed.substring(0, eq).trim();
            String value = trimmed.substring(eq + 1).trim();
            if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                value = value.substring(1, value.length() - 1);
            }
            map.put(key, value);
        }
        return map;
    }
}