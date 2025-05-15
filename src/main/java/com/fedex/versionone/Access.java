package com.fedex.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.versionone.apiclient.exceptions.V1Exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Access {
    public static void main(String[] args) throws V1Exception, IOException, InterruptedException {
        StoryFetcher storyFetcher = new StoryFetcher();
        String team = PropertyFetcher.getProperty("v1.team");
        List<String> storyIds = storyFetcher.getStoriesForTeam(team);

        ActivityFetcher activityFetcher = new ActivityFetcher();

        List<Story> stories = new ArrayList<>();

        for (String storyId : storyIds) {
            JsonNode storyRoot = activityFetcher.GetActivity(storyId);

            StoryParser storyParser = new StoryParser(storyRoot);
            String startDate = storyParser.findStartDate();
            String endDate = storyParser.findEndDate();

            stories.add(new Story(storyId.substring(6), startDate, endDate));
        }

        int i = 1;
    }
}
