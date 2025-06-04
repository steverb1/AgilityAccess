package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.versionone.apiclient.exceptions.V1Exception;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ActivityFetcherTest {
    String baseUrl = "https://example.com";
    String accessToken = "exampleAccessToken";

    @Test
    void getActivity_Stub() throws IOException, InterruptedException {
        HttpClientSpy httpClient = new HttpClientSpy();
        ActivityFetcher activityFetcher = new ActivityFetcher(httpClient, baseUrl, accessToken);
        String storyId = "Story:123";
        String urlString = baseUrl + "/api/ActivityStream/" + storyId;

        String body = "[{},{}]";
        httpClient.setBody(body);
        JsonNode storyRoot = activityFetcher.getActivity(storyId);

        assertThat(storyRoot.toString()).isEqualTo(body);
        assertThat(httpClient.lastRequest.uri().toString()).isEqualTo(urlString);
        assertThat(httpClient.lastRequest.headers().map().toString()).isEqualTo("{Accept=[application/json], Authorization=[Bearer " + accessToken + "]}");
    }

    @Test
    void getStoriesForTeam() throws IOException, InterruptedException {
        HttpClientSpy httpClient = new HttpClientSpy();
        ActivityFetcher activityFetcher = new ActivityFetcher(httpClient, baseUrl, accessToken);
        String teamOid = "Team:123";
        String fromClosedDate = "2025-05-01";

        String body = """
                {
                  "Assets": [
                    {
                      "id": "Story:1234"
                    }
                  ]
                }
                """;
        httpClient.setBody(body);

        List<String> storyIds = activityFetcher.getStoriesForTeam(teamOid, fromClosedDate);

        assertThat(storyIds.size()).isEqualTo(1);
        assertThat(httpClient.lastRequest.uri().toString()).isEqualTo("https://example.com/rest-1.v1/Data/Story?sel=ID&where=ClosedDate%3E%272025-05-01T00%3A00%3A00Z%27%3BTeam%3D%27Team%3A123%27");
    }

    @Test
    void getTeamName_WhenTeamExists_ReturnsTeamName() throws Exception {
        HttpClientSpy httpClient = new HttpClientSpy();
        ActivityFetcher activityFetcher = new ActivityFetcher(httpClient, baseUrl, accessToken);
        String expectedTeamName = "Test Team";
        httpClient.setBody(
            """
            {
                "Assets": [{
                    "Attributes": {
                        "Name": {
                            "value": "%s"
                        }
                    }
                }]
            }""".formatted(expectedTeamName)
        );

        String actualTeamName = activityFetcher.getTeamName("Team:1234");

        assertThat(actualTeamName).isEqualTo(expectedTeamName);
        assertThat(httpClient.lastRequest.uri().toString()).isEqualTo(baseUrl + "/rest-1.v1/Data/Team?where=ID='Team:1234'&sel=Name");
        assertThat(httpClient.lastRequest.headers().map().toString()).isEqualTo("{Accept=[application/json], Authorization=[Bearer " + accessToken + "]}");
    }

    @Test
    void getTeamName_WhenTeamNotFound_ReturnsEmptyString() throws IOException, InterruptedException {
        HttpClientSpy httpClient = new HttpClientSpy();
        ActivityFetcher activityFetcher = new ActivityFetcher(httpClient, baseUrl, accessToken);
        httpClient.setBody(
            """
            {
                "Assets": []
            }"""
        );

        String teamName = activityFetcher.getTeamName("Team:9999");

        assertThat(teamName).isEmpty();
    }

    @Test
    void getTeamsToProcess_WhenScopeAndTeamAreNull_ReturnsEmptyMap() throws IOException, V1Exception, InterruptedException {
        HttpClientSpy httpClient = new HttpClientSpy();
        ActivityFetcher activityFetcher = new ActivityFetcher(httpClient, baseUrl, accessToken);

        Map<String, String> result = activityFetcher.getTeamsToProcess(null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void getTeamsToProcess_WhenScopeIsNull_ReturnsSingleTeam() throws IOException, V1Exception, InterruptedException {
        HttpClientSpy httpClient = new HttpClientSpy();
        ActivityFetcher activityFetcher = new ActivityFetcher(httpClient, baseUrl, accessToken);
        String teamOid = "Team:1234";
        String teamName = "Test Team";
        httpClient.setBody(
            """
            {
                "Assets": [{
                    "Attributes": {
                        "Name": {
                            "value": "%s"
                        }
                    }
                }]
            }""".formatted(teamName)
        );

        Map<String, String> result = activityFetcher.getTeamsToProcess(null, teamOid);

        assertThat(result).hasSize(1);
        assertThat(result).containsEntry(teamOid, teamName);
    }

    //@Test
    void getTeamsToProcess_WhenScopeIsNotNull_ReturnsTeamsForScope() throws IOException, V1Exception, InterruptedException {
        HttpClientSpy httpClient = new HttpClientSpy();
        ActivityFetcher activityFetcher = new ActivityFetcher(httpClient, baseUrl, accessToken);
        String scopeOid = "Scope:1234";
        String teamOid1 = "Team:1234";
        String teamName1 = "Test Team 1";
        String teamOid2 = "Team:5678";
        String teamName2 = "Test Team 2";

        httpClient.setBody(
            """
            {
                "Assets": [{
                    "ID": "%s",
                    "Attributes": {
                        "Name": {
                            "value": "%s"
                        }
                    }
                }, {
                    "ID": "%s",
                    "Attributes": {
                        "Name": {
                            "value": "%s"
                        }
                    }
                }]
            }""".formatted(teamOid1, teamName1, teamOid2, teamName2)
        );

        Map<String, String> result = activityFetcher.getTeamsToProcess(scopeOid, null);

        assertThat(result).hasSize(2);
        assertThat(result).containsEntry(teamOid1, teamName1);
        assertThat(result).containsEntry(teamOid2, teamName2);
    }
}
