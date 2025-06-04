package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ActivityFetcher {

    private final ForHttpClientCalls httpClient;
    private final String accessToken;
    String baseUrl;

    ActivityFetcher(ForHttpClientCalls httpClient, String baseUrl1, String accessToken) throws IOException {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl1;
        this.accessToken = accessToken;
    }

    public ActivityFetcher() throws IOException {
        this(new HttpClientWrapper(),
             PropertyFetcher.getProperty("v1.url"),
             PropertyFetcher.getProperty("v1.token"));
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

    public List<String> getStoriesForTeam(String teamOid, String fromClosedDate) throws IOException, InterruptedException {
        String fullDate = fromClosedDate + "T00:00:00Z";
        String whereClause = String.format("ClosedDate>'%s';Team='%s'", fullDate, teamOid);
        String encodedWhere = URLEncoder.encode(whereClause, StandardCharsets.UTF_8);

        String fullUrl = String.format("%s/rest-1.v1/Data/Story?sel=ID&where=%s", baseUrl, encodedWhere);

        JsonNode root = sendHttpRequest(fullUrl);

        JsonNode assets = root.get("Assets");

        List<String> stories = new ArrayList<>();
        for (JsonNode asset : assets) {
            String storyId = asset.get("id").asText();
            stories.add(storyId);
        }

        return stories;
    }

    Map<String, String> getTeamsToProcess(String scopeOid, String teamOid) throws IOException, InterruptedException {
        Map<String, String> teamOidToTeamName;

        if (scopeOid == null  || scopeOid.isEmpty()) {
            if (teamOid == null || teamOid.isEmpty()) {
                return new HashMap<>();
            }

            String teamName = getTeamName(teamOid);
            teamOidToTeamName = new HashMap<>();
            teamOidToTeamName.put(teamOid, teamName);
        }
        else {
            teamOidToTeamName = getTeamsForScope(scopeOid);
        }
        return teamOidToTeamName;
    }

    String getTeamName(String teamOidString) throws IOException, InterruptedException {
        String urlString = String.format("%s/rest-1.v1/Data/Team?where=ID='%s'&sel=Name", baseUrl, teamOidString);

        JsonNode root = sendHttpRequest(urlString);
        JsonNode assets = root.get("Assets");
        if (assets != null && !assets.isEmpty()) {
            return assets.get(0).get("Attributes").get("Name").get("value").asText();
        }
        return "";
    }

    private Map<String, String> getTeamsForScope(String scope) throws IOException, InterruptedException {
        String urlString = String.format("%s/rest-1.v1/Data/Team?where=Scope.ID='%s'&sel=Name", baseUrl, scope);

        JsonNode root = sendHttpRequest(urlString);
        JsonNode teams = root.get("Assets");

        Map<String, String> teamOidToTeamName = new HashMap<>();
        if (teams != null) {
            for (JsonNode team : teams) {
                String id = team.get("id").asText();
                String name = team.get("Attributes").get("Name").get("value").asText();
                teamOidToTeamName.put(id, name);
            }
        }

        return teamOidToTeamName;
    }

    private JsonNode sendHttpRequest(String fullUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readTree(response.body());
    }
}
