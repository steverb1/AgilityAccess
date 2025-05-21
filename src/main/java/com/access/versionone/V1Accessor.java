package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.versionone.apiclient.exceptions.V1Exception;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class V1Accessor {
    public static void main(String[] args) throws V1Exception, IOException, InterruptedException {
        StoryFetcher storyFetcher = new StoryFetcher();
        String teamOidString = PropertyFetcher.getProperty("v1.team");
        List<String> storyIds = storyFetcher.getStoriesForTeam(teamOidString);
        List<Float> storyPoints = new ArrayList<>();

        ActivityFetcher activityFetcher = new ActivityFetcher();
        List<StoryHistory> histories = new ArrayList<>();

        for (String storyId : storyIds) {
            JsonNode storyRoot = activityFetcher.GetActivity(storyId);

            StoryParser storyParser = new StoryParser(storyRoot);

            Map<String, LocalDate> storyDates = storyParser.findStateChangeDates();
            StoryHistory storyHistory = new StoryHistory(storyId, storyDates);
            histories.add(storyHistory);

            if (PropertyFetcher.getProperty("includeStoryPoints").equals("true")) {
                storyPoints.add(storyParser.findStoryEstimate());
            }
        }

        TeamFetcher teamFetcher = new TeamFetcher();
        String teamName = teamFetcher.getTeamName(teamOidString);

        OutputGenerator outputGenerator = new OutputGenerator();
        outputGenerator.createCsvFile(histories, storyPoints, teamName);
    }
}
