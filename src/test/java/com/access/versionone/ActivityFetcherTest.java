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
import static org.mockito.Mockito.*;

public class ActivityFetcherTest {
    private final String baseUrl = PropertyFetcher.getProperty("v1.url");
    private final String accessToken = PropertyFetcher.getProperty("v1.token");

    public ActivityFetcherTest() throws IOException {
    }

    @Test
    void activityFetcher_Stubbed() throws IOException, InterruptedException {
        StubbedHttpClient httpClient = new StubbedHttpClient();
        ActivityFetcher activityFetcher = new ActivityFetcher(httpClient);
        String storyId = "Story:123";
        String urlString = baseUrl + "/api/ActivityStream/" + storyId;

        String body = "[{},{}]";
        httpClient.setBody(body);

        JsonNode storyRoot = activityFetcher.getActivity(storyId, PropertyFetcher.getProperty("v1.url"), PropertyFetcher.getProperty("v1.token"));

        assertThat(storyRoot.toString()).isEqualTo(body);
        assertThat(httpClient.lastRequest.uri().toString()).isEqualTo(urlString);
        assertThat(httpClient.lastRequest.headers().map().toString()).isEqualTo("{Accept=[application/json], Authorization=[Bearer " + accessToken + "]}");
    }

    @Test
    void activityFetcher_Mocked() throws IOException, InterruptedException {
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
        HttpResponse<String> response = buildResponse(body);

        when(mockClient.send(request, HttpResponse.BodyHandlers.ofString())).thenReturn(response);
        ActivityFetcher activityFetcher = new ActivityFetcher(mockClient);

        JsonNode storyRoot = activityFetcher.getActivity(storyId, baseUrl, accessToken);
        assertThat(storyRoot.toString()).isEqualTo(body);
    }

    @Test
    void ActivityFetcher_Wrapper() {

    }

    private HttpResponse<String> buildResponse(String body) {
        return new HttpResponse<>() {
            @Override public int statusCode() {return 0;}
            @Override public HttpRequest request() {return null;}
            @Override public Optional<HttpResponse<String>> previousResponse() {return Optional.empty();}
            @Override public HttpHeaders headers() {return null;}
            @Override public String body() {return body;}
            @Override public Optional<SSLSession> sslSession() {return Optional.empty();}
            @Override public URI uri() {return null;}
            @Override public HttpClient.Version version() {return null;}
        };
    }
}

class StubbedHttpClient implements ForHttpClientCalls {
    HttpRequest lastRequest;
    String body;

    void setBody(String body) {
        this.body = body;
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        lastRequest = request;

        return new HttpResponse<>() {
            @Override public int statusCode() { return 0; }
            @Override public HttpRequest request() { return null; }
            @Override public Optional<HttpResponse<T>> previousResponse() {return Optional.empty();}
            @Override public HttpHeaders headers() {return null;}
            @Override public T body() {return (T) body;}
            @Override public Optional<SSLSession> sslSession() {return Optional.empty();}
            @Override public URI uri() {return null;}
            @Override public HttpClient.Version version() {return null;}
        };
    }
}
