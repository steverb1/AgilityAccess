package com.access.versionone;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivityFetcherIT {
    ForHttpClientCalls httpClient = new HttpClientReal();
    HttpClientWrapper clientWrapper = new HttpClientWrapper(PropertyFetcher.getProperty("v1.token"), httpClient);
    ActivityFetcher activityFetcher = new ActivityFetcher(clientWrapper, PropertyFetcher.getProperty("v1.url"));

    public ActivityFetcherIT() throws IOException {

    }

    @Test
    void fetchAllDataForScope() throws IOException, InterruptedException {
        Map<String, String> teamsToProcess = activityFetcher.getTeamsToProcess("Scope:1005", null);
        assertThat(teamsToProcess).isNotEmpty();

        List<StoryHistory> storyHistories = activityFetcher.getStoryHistories(teamsToProcess, true, true, PropertyFetcher.getProperty("fromClosedDate"), PropertyFetcher.getProperty("states"));
        assertThat(storyHistories).isNotEmpty();
    }

    @Test
    void fetchAllDataForTeam() throws IOException, InterruptedException {
        Map<String, String> teamsToProcess = activityFetcher.getTeamsToProcess(null, "Team:1889");
        assertThat(teamsToProcess).isNotEmpty();

        List<StoryHistory> storyHistories = activityFetcher.getStoryHistories(teamsToProcess, true, true, PropertyFetcher.getProperty("fromClosedDate"), PropertyFetcher.getProperty("states"));
        assertThat(storyHistories).isNotEmpty();
    }

    @Test
    void getStoriesForTeam_NoClosedDate() throws IOException, InterruptedException {
        String teamOid = "Team:1889";
        List<String> storyIds = activityFetcher.getStoriesForTeam(teamOid, "");

        assertThat(storyIds).isNotEmpty();
    }
}
