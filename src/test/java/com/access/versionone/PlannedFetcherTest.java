package com.access.versionone;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class PlannedFetcherTest {
    String baseUrl = "https://example.com";
    String accessToken = "exampleAccessToken";
    HttpClientSpy httpClient = new HttpClientSpy();
    PlannedFetcher plannedFetcher = new PlannedFetcher(httpClient, baseUrl, accessToken);

    @Test
    void returnsStoriesFromStubbedHttpClient() throws IOException, InterruptedException {
        String timeboxOid = "Timebox:1050";
        String timeboxResponse =
            """
            {"Assets":[
                    {"Attributes":{"BeginDate":{"value":"2025-07-01"}}}
                ]}
            """;
        httpClient.addBody(timeboxResponse);

        String storiesResponse =
            """
            {"Assets":[
                {"Attributes":{"Name":{"value":"Story 1"},"ID":{"value":{"idref":"Story:111"}}}},
                {"Attributes":{"Name":{"value":"Story 2"},"ID":{"value":{"idref":"Story:222"}}}},
                {"Attributes":{"Name":{"value":"Story 3"},"ID":{"value":{"idref":"Story:333"}}}}
            ]}
            """;
        httpClient.addBody(storiesResponse);

        List<String> stories = plannedFetcher.getPlannedStories(timeboxOid);
        assertThat(stories).containsExactly(
                "Story 1 (Story:111)", "Story 2 (Story:222)", "Story 3 (Story:333)"
        );
        assertThat(httpClient.lastRequest.uri().toString())
            .isEqualTo("https://example.com/rest-1.v1/Data/PrimaryWorkitem?where=Timebox='Timebox:1050'&asof=2025-07-02&sel=Name,ID");
    }
}
