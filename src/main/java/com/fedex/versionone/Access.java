package com.fedex.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.versionone.apiclient.exceptions.V1Exception;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Access {
    public static void main(String[] args) throws V1Exception, IOException, InterruptedException {
        StoryFetcher storyFetcher = new StoryFetcher();
        List<String> storyIds = storyFetcher.getStoriesForTeam("Team:707462");

        ActivityFetcher activityFetcher = new ActivityFetcher();

        List<Story> stories = new ArrayList<>();

        for (String storyId : storyIds) {
            JsonNode storyRoot = activityFetcher.GetActivity(storyId);

            StoryParser storyParser = new StoryParser(storyRoot);
            LocalDate startDate = storyParser.findStartDate();
            LocalDate endDate = storyParser.findEndDate();

            stories.add(new Story(storyId.substring(6), startDate.toString(), endDate.toString()));
        }

        int i = 1;
    }
}
