package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StoryFetcher {
    ForHttpClientCalls httpClient = new HttpClientWrapper();

    List<StoryHistory> getStoryHistories(Map<String, String> teamOidToTeamName) throws IOException, InterruptedException {
        List<StoryHistory> histories = new ArrayList<>();
        ActivityFetcher activityFetcher = new ActivityFetcher(httpClient, PropertyFetcher.getProperty("v1.url"), PropertyFetcher.getProperty("v1.token"));

        for (String teamOid : teamOidToTeamName.keySet()) {
            List<String> storyIds = activityFetcher.getStoriesForTeam(teamOid, PropertyFetcher.getProperty("fromClosedDate"));

            Float storyPoints = null;
            String teamName = "";

            for (String storyId : storyIds) {
                JsonNode storyRoot = activityFetcher.getActivity(storyId);

                StoryParser storyParser = new StoryParser(storyRoot);
                Map<String, LocalDate> storyDates = storyParser.findStateChangeDates();

                if (PropertyFetcher.getProperty("includeStoryPoints").equals("true")) {
                    storyPoints = storyParser.findStoryEstimate();
                }
                if (PropertyFetcher.getProperty("includeTeamName").equals("true")) {
                    teamName = teamOidToTeamName.get(teamOid);
                }

                StoryHistory storyHistory = new StoryHistory(storyId, storyDates, storyPoints, teamName);
                histories.add(storyHistory);
            }
        }

        return histories;
    }
}
