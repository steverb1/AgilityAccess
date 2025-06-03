package com.access.versionone;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TeamFetcherTest {
    @Test
    void getTeamName_ReturnsTeamName_WhenTeamExists() throws Exception {
        HttpClientSpy httpClient = new HttpClientSpy();
        String expectedTeamName = "Test Team";
        httpClient.setBody("""
            {
                "Assets": [{
                    "Attributes": {
                        "Name": {
                            "value": "Test Team"
                        }
                    }
                }]
            }""");

        TeamFetcher teamFetcher = new TeamFetcher(httpClient, "http://v1host", "fake-token");

        String actualTeamName = teamFetcher.getTeamName("Team:1234");

        assertEquals(expectedTeamName, actualTeamName);
        assertEquals("http://v1host/rest-1.v1/Data/Team?where=ID='Team:1234'&sel=Name",
                httpClient.lastRequest.uri().toString());
        assertTrue(httpClient.lastRequest.headers().firstValue("Authorization")
                .orElse("").contains("fake-token"));
    }

    @Test
    void getTeamName_ThrowsException_WhenTeamNotFound() {
        // Arrange
        HttpClientSpy httpClient = new HttpClientSpy();
        httpClient.setBody("""
            {
                "Assets": []
            }""");

        TeamFetcher teamFetcher = new TeamFetcher(httpClient, "http://v1host", "fake-token");

        // Act & Assert
        Exception exception = assertThrows(IOException.class, () ->
            teamFetcher.getTeamName("Team:9999")
        );
        assertTrue(exception.getMessage().contains("Team not found"));
    }
}
