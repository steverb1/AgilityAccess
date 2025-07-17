package com.access.versionone;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PlannedFetcherIT {
    private final String accessToken = PropertyFetcher.getProperty("v1.token");

    PlannedFetcherIT() throws IOException {
    }

    @Test
    void fetchStoriesForIteration() throws IOException, InterruptedException {
        PlannedFetcher plannedFetcher = new PlannedFetcher(new HttpClientWrapper(accessToken),
                PropertyFetcher.getProperty("v1.url")
        );

        List<String> stories = plannedFetcher.getPlannedStories("Timebox:9857");
        assertThat(stories.size()).isEqualTo(3);
    }

    @Test
    void getIterationActivationDateTime() throws IOException, InterruptedException {
        PlannedFetcher plannedFetcher = new PlannedFetcher(new HttpClientWrapper(accessToken),
                PropertyFetcher.getProperty("v1.url")
        );

        String startDate = plannedFetcher.getIterationActivationDate("Timebox:1050");
        assertThat(startDate).isEqualTo("2017-03-07T05:11:17.087");
    }

    @Test
    void getAllIterationsAfterDate() throws IOException, InterruptedException {
        PlannedFetcher plannedFetcher = new PlannedFetcher(new HttpClientWrapper(accessToken),
                PropertyFetcher.getProperty("v1.url")
        );

        List<Iteration> iterations = plannedFetcher.getAllIterationsAfterDate("2025-01-01");
        assertThat(iterations.size()).isGreaterThan(0);
    }
}
