package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.versionone.Oid;
import com.versionone.apiclient.*;
import com.versionone.apiclient.exceptions.V1Exception;
import com.versionone.apiclient.filters.FilterTerm;
import com.versionone.apiclient.interfaces.IAssetType;
import com.versionone.apiclient.interfaces.IAttributeDefinition;
import com.versionone.apiclient.services.QueryResult;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        JsonNode assets = root.get("Assets");

        List<String> stories = new ArrayList<>();
        for (JsonNode asset : assets) {
            String storyId = asset.get("id").asText();
            stories.add(storyId);
        }

        return stories;
    }

    Map<String, String> getTeamsToProcess(String scopeOid, String teamOid) throws IOException, V1Exception, InterruptedException {
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

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode assets = root.get("Assets");
        if (assets != null && !assets.isEmpty()) {
            return assets.get(0).get("Attributes").get("Name").get("value").asText();
        }
        return "";
    }

    private Map<String, String> getTeamsForScope(String scope) throws V1Exception, IOException {
        Connector connector = new Connector();
        V1Connector v1Connector = connector.buildV1Connector();
        MetaModel metaModel = new MetaModel(v1Connector);
        Services services = new Services(v1Connector);

        Oid scopeOid = Oid.fromToken(scope, metaModel);

        IAttributeDefinition teamNameAttr = metaModel.getAttributeDefinition("Team.Name");

        IAssetType teamRoomType = metaModel.getAssetType("TeamRoom");
        Query teamRoomQuery = new Query(teamRoomType);
        IAttributeDefinition teamRoomTeamAttr = metaModel.getAttributeDefinition("TeamRoom.Team");
        teamRoomQuery.getSelection().add(teamRoomTeamAttr);

        IAttributeDefinition teamRoomScopeAttr = metaModel.getAttributeDefinition("TeamRoom.Scope");
        FilterTerm scopeFilter = new FilterTerm(teamRoomScopeAttr);
        scopeFilter.equal(scopeOid);
        teamRoomQuery.setFilter(scopeFilter);

        QueryResult teamRoomResult = services.retrieve(teamRoomQuery);

        List<Oid> teamOids = new ArrayList<>();
        for (Asset teamRoom : teamRoomResult.getAssets()) {
            Oid teamOid = (Oid) teamRoom.getAttribute(teamRoomTeamAttr).getValue();
            if (teamOid != null) {
                teamOids.add(teamOid);
            }
        }

        Map<String, String> teamOidToTeamName = new HashMap<>();

        for (Oid teamOid : teamOids) {
            Query teamQuery = new Query(teamOid);
            teamQuery.getSelection().add(teamNameAttr);
            QueryResult teamResult = services.retrieve(teamQuery);
            Asset team = teamResult.getAssets()[0];
            String teamName = team.getAttribute(teamNameAttr).getValue().toString();

            teamOidToTeamName.put(teamOid.toString(), teamName);
        }

        return teamOidToTeamName;
    }
}
