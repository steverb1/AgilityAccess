package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlannedFetcher {
    private final HttpClientWrapper httpClient;
    String baseUrl;

    PlannedFetcher(HttpClientWrapper httpClient, String baseUrl1) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl1;
    }

    public List<String> getPlannedStories(String timeboxOid) throws IOException, InterruptedException {
        String iterationActivationDate = getIterationActivationDate(timeboxOid);
        String urlString = String.format("%s/rest-1.v1/Data/PrimaryWorkitem?where=Timebox='%s'&asof=%s&sel=Name,ID",
                baseUrl, timeboxOid, iterationActivationDate);
        JsonNode root = httpClient.sendHttpRequest(urlString);
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

    public List<Iteration> getAllIterationsAfterDate(String date) throws IOException, InterruptedException {
        String whereClause = String.format("BeginDate>'%s'", date);
        String encodedWhere = java.net.URLEncoder.encode(whereClause, java.nio.charset.StandardCharsets.UTF_8);
        String urlString = String.format("%s/rest-1.v1/Data/Timebox?where=%s&sel=Name", baseUrl, encodedWhere);

        JsonNode root = httpClient.sendHttpRequest(urlString);
        JsonNode assets = root.get("Assets");

        ArrayList<Iteration> iterations = new ArrayList<>();

        if (assets != null) {
            for (JsonNode asset : assets) {
                String oid = asset.get("id").asText();
                JsonNode attributes = asset.get("Attributes");
                String name = attributes.get("Name").get("value").asText();

                Iteration iteration = new Iteration(name, oid);
                iterations.add(iteration);
            }
        }

        return iterations;
    }

    String getIterationActivationDate(String timeboxOid) throws IOException, InterruptedException {
        String urlString = String.format("%s/rest-1.v1/Hist/Timebox?where=ID='%s';State.Code='ACTV'&sel=ChangeDate,State", baseUrl, timeboxOid);
        JsonNode root = httpClient.sendHttpRequest(urlString);
        JsonNode assets = root.get("Assets");
        for (JsonNode asset : assets) {
            JsonNode attributes = asset.get("Attributes");
            return attributes.get("ChangeDate").get("value").asText();
        }
        return "";
    }
}
