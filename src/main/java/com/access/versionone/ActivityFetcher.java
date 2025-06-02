package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ActivityFetcher {

    private final ForHttpClientCalls httpClient;
    private final String accessToken;
    String baseUrl;

    ActivityFetcher(ForHttpClientCalls httpClient, String baseUrl1, String accessToken) throws IOException {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl1;
        this.accessToken = accessToken;
    }

    JsonNode getActivity(String workItemId) throws IOException, InterruptedException {
        String urlString = baseUrl + "/api/ActivityStream/" + workItemId;

        HttpResponse<String> response;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(body);
    }
}
