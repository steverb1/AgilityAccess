package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivityFetcherTest {
    String baseUrl = "https://example.com";
    String accessToken = "exampleAccessToken";
    HttpClientSpy httpClient = new HttpClientSpy();
    HttpClientWrapper httpClientWrapper = new HttpClientWrapper(accessToken, httpClient);
    ActivityFetcher activityFetcher = new ActivityFetcher(httpClientWrapper, baseUrl);

    @Test
    void getActivity() throws IOException, InterruptedException {
        String storyId = "Story:123";
        String urlString = baseUrl + "/api/ActivityStream/" + storyId;

        String body = "[{},{}]";
        httpClient.addBody(body);
        JsonNode storyRoot = activityFetcher.getActivity(storyId);

        assertThat(storyRoot.toString()).isEqualTo(body);
        assertThat(httpClient.lastRequest.uri().toString()).isEqualTo(urlString);
        assertThat(httpClient.lastRequest.headers().map().toString()).isEqualTo("{Accept=[application/json], Authorization=[Bearer " + accessToken + "]}");
    }

    @Test
    void getStoriesForTeam() throws IOException, InterruptedException {
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
        httpClient.addBody(body);

        List<String> storyIds = activityFetcher.getStoriesForTeam(WorkItemType.Story, teamOid, fromClosedDate);

        assertThat(storyIds.size()).isEqualTo(1);
        assertThat(httpClient.lastRequest.uri().toString()).isEqualTo("https://example.com/rest-1.v1/Data/Story?sel=ID&where=ClosedDate%3E'2025-05-01T00:00:00Z';Team='Team:123'");
    }

    @Test
    void getStoriesForTeam_NoClosedDate() throws IOException, InterruptedException {
        String teamOid = "Team:123";
        String fromClosedDate = "";

        String body = """
                {
                  "Assets": [
                    {
                      "id": "Story:1234"
                    }
                  ]
                }
                """;
        httpClient.addBody(body);

        List<String> storyIds = activityFetcher.getStoriesForTeam(WorkItemType.Story, teamOid, fromClosedDate);

        assertThat(storyIds.size()).isEqualTo(1);
        assertThat(httpClient.lastRequest.uri().toString()).isEqualTo("https://example.com/rest-1.v1/Data/Story?sel=ID&where=Team='Team:123'");
    }

    @Test
    void getTeamName_WhenTeamExists_ReturnsTeamName() throws Exception {
        String expectedTeamName = "Test Team";
        httpClient.addBody(
            """
            {
                "Assets": [{
                    "Attributes": {
                        "Name": {
                            "value": "%s"
                        }
                    }
                }]
            }""".formatted(expectedTeamName)
        );

        String actualTeamName = activityFetcher.getTeamName("Team:1234");

        assertThat(actualTeamName).isEqualTo(expectedTeamName);
        assertThat(httpClient.lastRequest.uri().toString()).isEqualTo(baseUrl + "/rest-1.v1/Data/Team?where=ID='Team:1234'&sel=Name");
        assertThat(httpClient.lastRequest.headers().map().toString()).isEqualTo("{Accept=[application/json], Authorization=[Bearer " + accessToken + "]}");
    }

    @Test
    void getTeamName_WhenTeamNotFound_ReturnsEmptyString() throws IOException, InterruptedException {
        httpClient.addBody(
            """
            {
                "Assets": []
            }"""
        );

        String teamName = activityFetcher.getTeamName("Team:9999");

        assertThat(teamName).isEmpty();
    }

    @Test
    void getTeamsToProcess_WhenScopeAndTeamAreNull_ReturnsEmptyMap() throws IOException, InterruptedException {
        Map<String, String> result = activityFetcher.getTeamsToProcess(null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void getTeamsToProcess_WhenScopeIsNull_ReturnsSingleTeam() throws IOException, InterruptedException {
        String teamOid = "Team:1234";
        String teamName = "Test Team";
        httpClient.addBody(
            """
            {
                "Assets": [{
                    "Attributes": {
                        "Name": {
                            "value": "%s"
                        }
                    }
                }]
            }""".formatted(teamName)
        );

        Map<String, String> result = activityFetcher.getTeamsToProcess(null, teamOid);

        assertThat(result).hasSize(1);
        assertThat(result).containsEntry(teamOid, teamName);
    }

    @Test
    void getTeamsToProcess_WhenScopeIsNotNull_ReturnsTeamsForScope() throws IOException, InterruptedException {
        String scopeOid = "Scope:1234";
        String teamOid1 = "Team:1234";
        String teamName1 = "Test Team 1";
        String teamOid2 = "Team:5678";
        String teamName2 = "Test Team 2";

        httpClient.addBody(
            """
            {
                "Assets": [{
                    "id": "%s",
                    "Attributes": {
                        "Name": {
                            "value": "%s"
                        }
                    }
                }, {
                    "id": "%s",
                    "Attributes": {
                        "Name": {
                            "value": "%s"
                        }
                    }
                }]
            }""".formatted(teamOid1, teamName1, teamOid2, teamName2)
        );

        Map<String, String> result = activityFetcher.getTeamsToProcess(scopeOid, null);

        assertThat(result).hasSize(2);
        assertThat(result).containsEntry(teamOid1, teamName1);
        assertThat(result).containsEntry(teamOid2, teamName2);
    }
    
    @Test
    void getStoryHistories() throws IOException, InterruptedException {
        Map<String, String> teamOidToTeamName = new HashMap<>();
        teamOidToTeamName.put("Team:1234", "Team Bob");

        httpClient.addBody(
            """
            {
              "_type" : "Assets",
              "total" : 2,
              "Assets" : [ {
                "id" : "Story:9849"
              }, {
                "id" : "Story:9852"
              } ]
            }
            """
        );

        httpClient.addBody(
            """
            [ {
             "body" : {
               "time" : "2025-06-03T16:18:13.177Z",
               "target" : [ {
                 "name" : "Status",
                 "newValue" : "Ready for Build"
               } ]
             }
           }, {
             "body" : {
               "time" : "2025-06-04T16:18:13.177Z",
               "target" : [ {
                 "name" : "Status",
                 "newValue" : "Build"
               } ]
             }
           }, {
             "body" : {
               "time" : "2025-06-05T16:18:13.177Z",
               "target" : [ {
                 "name" : "Status",
                 "newValue" : "Done"
               } ]
             }
           } ]
           """
        );

        httpClient.addBody(
                """
                [ {
                 "body" : {
                   "time" : "2025-08-03T16:18:13.177Z",
                   "target" : [ {
                     "name" : "Status",
                     "newValue" : "Ready for Build"
                   },{
                     "name" : "Estimate",
                     "newValue" : "5.00"
                   } ]
                 }
               }, {
                 "body" : {
                   "time" : "2025-08-04T16:18:13.177Z",
                   "target" : [ {
                     "name" : "Status",
                     "newValue" : "Build"
                   } ]
                 }
               }, {
                 "body" : {
                   "time" : "2025-08-05T16:18:13.177Z",
                   "target" : [ {
                     "name" : "Status",
                     "newValue" : "Done"
                   } ]
                 }
               } ]
               """
        );

        List<StoryHistory> storyHistories = activityFetcher.getStoryHistories(teamOidToTeamName, true, true,
                "2025-01-01", "Ready for Build, Build, Done");

        assertThat(storyHistories.size()).isEqualTo(2);

        StoryHistory storyHistory1 = storyHistories.getFirst();
        assertThat(storyHistory1.storyId()).isEqualTo("Story:9849");
        assertThat(storyHistory1.stateDates().size()).isEqualTo(3);
        assertThat(storyHistory1.stateDates().get("Ready for Build")).isEqualTo("2025-06-03");
        assertThat(storyHistory1.stateDates().get("Build")).isEqualTo("2025-06-04");
        assertThat(storyHistory1.stateDates().get("Done")).isEqualTo("2025-06-05");
        assertThat(storyHistory1.storyPoints()).isNull();
        assertThat(storyHistory1.teamName()).isEqualTo("Team Bob");

        assertThat(storyHistories.get(1).storyPoints()).isEqualTo(5.0f);
    }
}
