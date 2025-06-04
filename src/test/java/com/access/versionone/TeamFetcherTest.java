package com.access.versionone;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class TeamFetcherTest {
    String baseUrl = "https://example.com";
    String accessToken = "exampleAccessToken";

    @Test
    void getTeamName_WhenTeamExists_ReturnsTeamName() throws Exception {
        HttpClientSpy httpClient = new HttpClientSpy();
        String expectedTeamName = "Test Team";
        httpClient.setBody(
            """
            {
                "Assets": [{
                    "Attributes": {
                        "Name": {
                            "value": "Test Team"
                        }
                    }
                }]
            }"""
        );

        TeamFetcher teamFetcher = new TeamFetcher(httpClient, baseUrl, accessToken);

        String actualTeamName = teamFetcher.getTeamName("Team:1234");

        assertThat(actualTeamName).isEqualTo(expectedTeamName);
        assertThat(httpClient.lastRequest.uri().toString())
                .isEqualTo("https://example.com/rest-1.v1/Data/Team?where=ID='Team:1234'&sel=Name");
        assertThat(httpClient.lastRequest.headers().map().toString()).isEqualTo("{Accept=[application/json], Authorization=[Bearer " + accessToken + "]}");
    }

    @Test
    void getTeamName_WhenTeamNotFound_ReturnsEmptyString() throws IOException, InterruptedException {
        HttpClientSpy httpClient = new HttpClientSpy();
        httpClient.setBody(
            """
            {
                "Assets": []
            }
            """
        );

        TeamFetcher teamFetcher = new TeamFetcher(httpClient, "http://v1host", "fake-token");

        String teamName = teamFetcher.getTeamName("Team:9999");

        assertThat(teamName).isEqualTo("");
    }
}
