package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class PlannedFetcher {
    private final ForHttpClientCalls httpClient;
    private final String accessToken;
    String baseUrl;

    PlannedFetcher(ForHttpClientCalls httpClient, String baseUrl1, String accessToken) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl1;
        this.accessToken = accessToken;
    }

    public List<String> getPlannedStories(String timeboxOid) throws IOException, InterruptedException {
        String iterationActivationDate = getIterationActivationDate(timeboxOid);
        String urlString = String.format("%s/rest-1.v1/Data/PrimaryWorkitem?where=Timebox='%s'&asof=%s&sel=Name,ID",
                baseUrl, timeboxOid, iterationActivationDate);
        JsonNode root = sendHttpRequest(urlString);
        JsonNode assets = root.get("Assets");
        List<String> stories = new ArrayList<>();
        if (assets != null && assets.isArray()) {
            for (JsonNode asset : assets) {
                JsonNode attributes = asset.get("Attributes");
                String name = null;
                String oid = null;
                if (attributes != null) {
                    JsonNode nameNode = attributes.get("Name");
                    if (nameNode != null && nameNode.has("value")) {
                        name = nameNode.get("value").asText();
                    }
                    JsonNode idNode = attributes.get("ID");
                    if (idNode != null && idNode.has("value")) {
                        oid = idNode.get("value").get("idref").asText();
                        String[] parts = oid.split(":");
                        oid = parts[0] + ":" + parts[1];
                    }
                }
                stories.add(String.format("%s (%s)", name, oid));
            }
        }

        return stories;
    }

    String getIterationActivationDate(String timeboxOid) throws IOException, InterruptedException {
        String urlString = String.format("%s/rest-1.v1/Hist/Timebox?where=ID='%s'&sel=ChangeDate,State", baseUrl, timeboxOid);
        JsonNode root = sendHttpRequest(urlString);
        JsonNode assets = root.get("Assets");
        for (JsonNode asset : assets) {
            JsonNode attributes = asset.get("Attributes");
            if (attributes.has("State.Code")) {
                String state = attributes.get("State.Code").get("value").asText();
                if (state.equals("ACTV")) {
                    JsonNode activationDateNode = attributes.get("ChangeDate");
                    if (activationDateNode != null && activationDateNode.has("value")) {
                        return activationDateNode.get("value").asText();
                    }
                }
            }
        }
        return "";
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
