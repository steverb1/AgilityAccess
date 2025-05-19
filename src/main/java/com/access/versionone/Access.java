package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.versionone.apiclient.exceptions.V1Exception;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Access {
    public static void main(String[] args) throws V1Exception, IOException, InterruptedException {
        StoryFetcher storyFetcher = new StoryFetcher();
        String team = PropertyFetcher.getProperty("v1.team");
        List<String> storyIds = storyFetcher.getStoriesForTeam(team);

        ActivityFetcher activityFetcher = new ActivityFetcher();
        List<StoryHistory> histories = new ArrayList<>();

        for (String storyId : storyIds) {
            JsonNode storyRoot = activityFetcher.GetActivity(storyId);

            StoryParser storyParser = new StoryParser(storyRoot);

            Map<String, LocalDate> storyDates = storyParser.findHistory(storyId);
            StoryHistory storyHistory = new StoryHistory(storyId, storyDates);
            histories.add(storyHistory);
        }

        OutputGenerator outputGenerator = new OutputGenerator();
        outputGenerator.createCsvFile(histories);
    }
}
