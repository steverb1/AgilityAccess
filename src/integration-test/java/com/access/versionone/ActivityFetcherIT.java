package com.access.versionone;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivityFetcherIT {
    private final String accessToken = PropertyFetcher.getProperty("v1.token");

    public ActivityFetcherIT() throws IOException {
    }

    @Test
    void fetchAllDataForScope() throws IOException, InterruptedException {
        ActivityFetcher activityFetcher = new ActivityFetcher(new HttpClientWrapper(accessToken),
                "https://www16.v1host.com/api-examples"
        );

        Map<String, String> teamsToProcess = activityFetcher.getTeamsToProcess("Scope:1005", null);
        assertThat(teamsToProcess).isNotEmpty();

        List<StoryHistory> storyHistories = activityFetcher.getStoryHistories(teamsToProcess, true, true, PropertyFetcher.getProperty("fromClosedDate"), PropertyFetcher.getProperty("states"));
        assertThat(storyHistories).isNotEmpty();
    }

    @Test
    void fetchAllDataForTeam() throws IOException, InterruptedException {
        ActivityFetcher activityFetcher = new ActivityFetcher(new HttpClientWrapper(accessToken),
                "https://www16.v1host.com/api-examples"
        );

        Map<String, String> teamsToProcess = activityFetcher.getTeamsToProcess(null, "Team:1889");
        assertThat(teamsToProcess).isNotEmpty();

        List<StoryHistory> storyHistories = activityFetcher.getStoryHistories(teamsToProcess, true, true, PropertyFetcher.getProperty("fromClosedDate"), PropertyFetcher.getProperty("states"));
        assertThat(storyHistories).isNotEmpty();
    }

    @Test
    void getStoriesForTeam_NoClosedDate() throws IOException, InterruptedException {
        ActivityFetcher activityFetcher = new ActivityFetcher(new HttpClientWrapper(accessToken),
                "https://www16.v1host.com/api-examples"
        );

        String teamOid = "Team:1889";
        List<String> storyIds = activityFetcher.getStoriesForTeam(teamOid, "");

        assertThat(storyIds).isNotEmpty();
    }
}
