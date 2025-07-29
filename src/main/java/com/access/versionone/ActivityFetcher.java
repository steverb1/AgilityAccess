package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class ActivityFetcher {
    private final HttpClientWrapper httpClient;
    String baseUrl;

    ActivityFetcher(HttpClientWrapper httpClient, String baseUrl1) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl1;
    }

    public Map<String, String> getTeamsToProcess(String scopeOid, String teamOid) throws IOException, InterruptedException {
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

    public List<StoryHistory> getStoryHistories(Map<String, String> teamOidToTeamName,
                                                boolean includeStoryPoints, boolean includeTeamName, String fromClosedDate, String configuredStates) throws IOException, InterruptedException {
        List<StoryHistory> histories = new ArrayList<>();

        for (String teamOid : teamOidToTeamName.keySet()) {
            List<String> storyIds = getStoriesForTeam(WorkItemType.Story, teamOid, fromClosedDate);

            Float storyPoints = null;
            String teamName = "";

            for (String storyId : storyIds) {
                JsonNode storyRoot = getActivity(storyId);

                StoryParser storyParser = new StoryParser(storyRoot, configuredStates);
                Map<String, LocalDate> storyDates = storyParser.findStateChangeDates();

                if (includeStoryPoints) {
                    storyPoints = storyParser.findStoryEstimate();
                }
                if (includeTeamName) {
                    teamName = teamOidToTeamName.get(teamOid);
                }

                StoryHistory storyHistory = new StoryHistory(storyId, storyDates, storyPoints, teamName);
                histories.add(storyHistory);
            }
        }

        return histories;
    }

    JsonNode getActivity(String workItemId) throws IOException, InterruptedException {
        String urlString = baseUrl + "/api/ActivityStream/" + workItemId;

        return httpClient.sendHttpRequest(urlString);
    }

    List<String> getStoriesForTeam(WorkItemType workItemType, String teamOid, String fromClosedDate) throws IOException, InterruptedException {
        String whereClause = "where=";

        if (!fromClosedDate.isEmpty()) {
            String urlEncodedGreaterThan = "%3E";
            whereClause += "ClosedDate" + urlEncodedGreaterThan + "'" + fromClosedDate + "T00:00:00Z';";
        }
        whereClause += "Team='" + teamOid + "'";

        String urlString = String.format("%s/rest-1.v1/Data/%s?sel=ID&%s", baseUrl, workItemType, whereClause);

        JsonNode root = httpClient.sendHttpRequest(urlString);

        JsonNode assets = root.get("Assets");

        List<String> stories = new ArrayList<>();
        for (JsonNode asset : assets) {
            String storyId = asset.get("id").asText();
            stories.add(storyId);
        }

        return stories;
    }

    String getTeamName(String teamOidString) throws IOException, InterruptedException {
        String urlString = String.format("%s/rest-1.v1/Data/Team?where=ID='%s'&sel=Name", baseUrl, teamOidString);

        JsonNode root = httpClient.sendHttpRequest(urlString);
        JsonNode assets = root.get("Assets");
        if (assets != null && !assets.isEmpty()) {
            return assets.get(0).get("Attributes").get("Name").get("value").asText();
        }
        return "";
    }

    private Map<String, String> getTeamsForScope(String scope) throws IOException, InterruptedException {
        String urlString = String.format("%s/rest-1.v1/Data/Team?where=Scope.ID='%s'&sel=Name", baseUrl, scope);

        JsonNode root = httpClient.sendHttpRequest(urlString);
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
}
