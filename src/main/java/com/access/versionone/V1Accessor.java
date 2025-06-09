package com.access.versionone;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class V1Accessor {
    public static void main(String[] args) throws IOException, InterruptedException {
        extractStoryActivity(PropertyFetcher.getPropertyMap());
    }

    public static void extractStoryActivity(Map<String, String> properties) throws IOException, InterruptedException {
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

        new OutputGenerator(new FileWriter("stories.csv")).createCsvFile(histories,
                properties.get("includeStoryPoints").equals("true"),
                properties.get("includeTeamName").equals("true"),
                properties.get("states"));
    }
}
