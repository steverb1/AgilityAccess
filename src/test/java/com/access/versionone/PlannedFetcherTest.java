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
    void returnsPlannedStories() throws IOException, InterruptedException {
        String timeboxOid = "Timebox:1050";
        String timeboxResponse =
            """
            {"Assets":[
                    {"Attributes":{"ChangeDate":{"value":"2017-03-07T05:11:17.087"},"State.Code":{"value":"ACTV"}}}
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
            .isEqualTo("https://example.com/rest-1.v1/Data/PrimaryWorkitem?where=Timebox='Timebox:1050'&asof=2017-03-07T05:11:17.087&sel=Name,ID");
    }

    @Test
    void returnsIterationActivationDate() throws IOException, InterruptedException {
        String timeboxOid = "Timebox:1050";
        String changeDateResponse =
            """
            {"Assets":[
                    {"Attributes":{"ChangeDate":{"value":"2017-03-07T05:11:17.087"},"State.Code":{"value":"ACTV"}}}
                ]}
            """;
        httpClient.addBody(changeDateResponse);

        String activationDate = plannedFetcher.getIterationActivationDate(timeboxOid);
        assertThat(activationDate).isEqualTo("2017-03-07T05:11:17.087");
    }

    @Test
    void returnsAllIterationsAfterDate() throws IOException, InterruptedException {
        String iterationResponse =
            """
            {
                "Assets": [
                  {
                    "id": "Timebox:1234",
                    "Attributes": {
                      "Name": {"value": "Sprint 42"},
                      "StartDate": {"value": "2025-07-02"},
                      "EndDate": {"value": "2025-07-15"}
                    }
                  },
                  {
                    "id": "Timebox:5678",
                    "Attributes": {
                      "Name": {"value": "Sprint 43"},
                      "StartDate": {"value": "2025-07-02"},
                      "EndDate": {"value": "2025-07-15"}
                    }
                  }
                ]
            }
            """;
        httpClient.addBody(iterationResponse);

        List<Iteration> iterations = plannedFetcher.getAllIterationsAfterDate("2025-07-01");
        assertThat(iterations).containsExactly(
                new Iteration("Sprint 42", "Timebox:1234"),
                new Iteration("Sprint 43", "Timebox:5678")
        );
    }
}
