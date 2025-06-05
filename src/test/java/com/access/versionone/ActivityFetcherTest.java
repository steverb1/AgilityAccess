package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.versionone.apiclient.exceptions.V1Exception;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivityFetcherTest {
    String baseUrl = "https://example.com";
    String accessToken = "exampleAccessToken";
    private final HttpClientSpy httpClient = new HttpClientSpy();
    private final ActivityFetcher activityFetcher = new ActivityFetcher(httpClient, baseUrl, accessToken);

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

        List<String> storyIds = activityFetcher.getStoriesForTeam(teamOid, fromClosedDate);

        assertThat(storyIds.size()).isEqualTo(1);
        assertThat(httpClient.lastRequest.uri().toString()).isEqualTo("https://example.com/rest-1.v1/Data/Story?sel=ID&where=ClosedDate%3E'2025-05-01T00:00:00Z';Team='Team:123'");
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
    void getTeamsToProcess_WhenScopeAndTeamAreNull_ReturnsEmptyMap() throws IOException, V1Exception, InterruptedException {
        Map<String, String> result = activityFetcher.getTeamsToProcess(null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void getTeamsToProcess_WhenScopeIsNull_ReturnsSingleTeam() throws IOException, V1Exception, InterruptedException {
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
    void getTeamsToProcess_WhenScopeIsNotNull_ReturnsTeamsForScope() throws IOException, V1Exception, InterruptedException {
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

        List<StoryHistory> storyHistories = activityFetcher.getStoryHistories(teamOidToTeamName, true, true);

        assertThat(storyHistories.size()).isEqualTo(2);

        assertThat(storyHistories.get(0).storyId()).isEqualTo("Story:9849");
        assertThat(storyHistories.get(0).stateDates().size()).isEqualTo(3);
        assertThat(storyHistories.get(0).stateDates().get("Ready for Build")).isEqualTo("2025-06-03");
        assertThat(storyHistories.get(0).stateDates().get("Build")).isEqualTo("2025-06-04");
        assertThat(storyHistories.get(0).stateDates().get("Done")).isEqualTo("2025-06-05");
        assertThat(storyHistories.get(0).storyPoints()).isNull();
        assertThat(storyHistories.get(0).teamName()).isEqualTo("Team Bob");

        assertThat(storyHistories.get(1).storyPoints()).isEqualTo(5.0f);
    }
}
