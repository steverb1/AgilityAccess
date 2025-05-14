package com.fedex.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.versionone.apiclient.exceptions.V1Exception;

import java.io.IOException;
import java.util.List;

public class Access {
    public static void main(String[] args) throws V1Exception, IOException, InterruptedException {
        ActivityFetcher activityFetcher = new ActivityFetcher();

        StoryFetcher storyFetcher = new StoryFetcher();
        List<String> stories = storyFetcher.getStoriesForTeam("Team:707462");

        for (String storyId : stories) {
            JsonNode root = activityFetcher.GetActivity(storyId);

            for (JsonNode node : root) {
                JsonNode body = node.get("body");
                JsonNode verb = body.get("verb");

                String verbText = verb.values().toString();
            }
        }
    }
}
