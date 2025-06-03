package com.access.versionone;

import com.versionone.Oid;
import com.versionone.apiclient.*;
import com.versionone.apiclient.exceptions.V1Exception;
import com.versionone.apiclient.filters.FilterTerm;
import com.versionone.apiclient.interfaces.IAssetType;
import com.versionone.apiclient.interfaces.IAttributeDefinition;
import com.versionone.apiclient.services.QueryResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class TeamFetcher {
    private final ForHttpClientCalls httpClient;
    private final String accessToken;
    private final String baseUrl;

    public TeamFetcher(ForHttpClientCalls httpClient, String baseUrl, String accessToken) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
    }

    public TeamFetcher() throws IOException {
        this(new HttpClientWrapper(),
             PropertyFetcher.getProperty("v1.url"),
             PropertyFetcher.getProperty("v1.token"));
    }

    Map<String, String> getTeamsToProcess() throws IOException, V1Exception, InterruptedException {
        Map<String, String> teamOidToTeamName;
        String scopeOid = PropertyFetcher.getProperty("v1.planningLevel");

        if (scopeOid == null  || scopeOid.isEmpty()) {
            String teamOid = PropertyFetcher.getProperty("v1.team");
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
        throw new IOException("Team not found: " + teamOidString);
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
