package com.access.versionone;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivityFetcherIT {
    @Test
    void getTeamsToProcess() throws IOException, InterruptedException {
        ActivityFetcher activityFetcher = new ActivityFetcher(new HttpClientWrapper(),
                "https://www16.v1host.com/api-examples",
                PropertyFetcher.getProperty("v1.token"));

        Map<String, String> teamsToProcess = activityFetcher.getTeamsToProcess("Scope:1005", null);

        assertThat(!teamsToProcess.isEmpty());
    }
}
