package com.access.versionone;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class V1Accessor {
    public static void main(String[] args) throws IOException, InterruptedException {
        ActivityFetcher activityFetcher = new ActivityFetcher(new HttpClientWrapper(),
                PropertyFetcher.getProperty("v1.url"), PropertyFetcher.getProperty("v1.token"));

        Map<String, String> teamOidToTeamName = activityFetcher.getTeamsToProcess(
                PropertyFetcher.getProperty("v1.planningLevel"),
                PropertyFetcher.getProperty("v1.team"));

        List<StoryHistory> histories = activityFetcher.getStoryHistories(teamOidToTeamName,
                PropertyFetcher.getProperty("includeStoryPoints").equals("true"), PropertyFetcher.getProperty("includeTeamName").equals("true"));

        new OutputGenerator(new FileWriter("stories.csv")).createCsvFile(histories,
                PropertyFetcher.getProperty("includeStoryPoints").equals("true"),
                PropertyFetcher.getProperty("includeTeamName").equals("true"));
    }
}
