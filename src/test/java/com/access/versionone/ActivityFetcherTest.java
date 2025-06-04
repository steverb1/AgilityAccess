package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void getActivity_Mock() throws IOException, InterruptedException {
        String storyId = "Story:123";
        String urlString = baseUrl + "/api/ActivityStream/" + storyId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();

        ForHttpClientCalls mockClient = mock(ForHttpClientCalls.class);
        String body = "[{},{}]";
        HttpResponse<String> response = new HttpResponse<>() {
            @Override public int statusCode() {return 200;}
            @Override public HttpRequest request() {return request;}
            @Override public Optional<HttpResponse<String>> previousResponse() {return Optional.empty();}
            @Override public String body() {return body;}
            @Override public Optional<SSLSession> sslSession() {return Optional.empty();}
            @Override public URI uri() {return null;}
            @Override public HttpClient.Version version() {return null;}
            @Override public HttpHeaders headers() {return null;}
        };

        when(mockClient.send(request, HttpResponse.BodyHandlers.ofString())).thenReturn(response);
        ActivityFetcher activityFetcher = new ActivityFetcher(mockClient, baseUrl, accessToken);

        JsonNode storyRoot = activityFetcher.getActivity(storyId);
        assertThat(storyRoot.toString()).isEqualTo(body);
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
                            "value": "Test Team"
                        }
                    }
                }]
            }"""
        );

        String actualTeamName = activityFetcher.getTeamName("Team:1234");

        assertThat(actualTeamName).isEqualTo(expectedTeamName);
        assertThat(httpClient.lastRequest.uri().toString())
                .isEqualTo(baseUrl + "/rest-1.v1/Data/Team?where=ID='Team:1234'&sel=Name");
        assertThat(httpClient.lastRequest.headers().firstValue("Authorization"))
                .hasValueSatisfying(auth -> assertThat(auth).contains(accessToken));
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
}
