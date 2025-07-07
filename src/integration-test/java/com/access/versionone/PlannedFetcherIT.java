package com.access.versionone;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PlannedFetcherIT {
    @Test
    void fetchStoriesForIteration() throws IOException, InterruptedException {
        PlannedFetcher plannedFetcher = new PlannedFetcher(new HttpClientWrapper(),
                PropertyFetcher.getProperty("v1.url"),
                PropertyFetcher.getProperty("v1.token"));

        List<String> stories = plannedFetcher.getPlannedStories("Timebox:1050");
        assertThat(stories.size()).isEqualTo(6);
    }
}
