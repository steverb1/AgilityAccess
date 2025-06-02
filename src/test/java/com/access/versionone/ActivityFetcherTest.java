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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActivityFetcherTest {
    String baseUrl = "https://example.com";
    String accessToken = "exampleAccessToken";

    @Test
    void getActivity_Stub() throws IOException, InterruptedException {
        HttpClientStub httpClient = new HttpClientStub();
        ActivityFetcher activityFetcher = new ActivityFetcher(httpClient, baseUrl, accessToken);;
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
}
