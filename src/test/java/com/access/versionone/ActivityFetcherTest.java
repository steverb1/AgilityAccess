package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivityFetcherTest {
    String baseUrl = "https://example.com";
    String accessToken = "exampleAccessToken";

    @Test
    void testOne() throws IOException, InterruptedException {
        HttpClientStub httpClient = new HttpClientStub();
        ActivityFetcher activityFetcher = new ActivityFetcher(httpClient);
        String storyId = "Story:123";
        String urlString = baseUrl + "/api/ActivityStream/" + storyId;

        String body = "[{},{}]";
        httpClient.setBody(body);
        JsonNode storyRoot = activityFetcher.getActivity(storyId, baseUrl, accessToken);

        assertThat(storyRoot.toString()).isEqualTo(body);
        assertThat(httpClient.lastRequest.uri().toString()).isEqualTo(urlString);
        assertThat(httpClient.lastRequest.headers().map().toString()).isEqualTo("{Accept=[application/json], Authorization=[Bearer " + accessToken + "]}");
    }
}
