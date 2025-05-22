package com.access.versionone;

import com.versionone.apiclient.exceptions.V1Exception;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class V1Accessor {
    public static void main(String[] args) throws V1Exception, IOException, InterruptedException {
        Map<String, String> teamOidToTeamName;

        TeamFetcher teamFetcher = new TeamFetcher();
        teamOidToTeamName = teamFetcher.getTeamsToProcess();

        StoryFetcher storyFetcher = new StoryFetcher();
        List<StoryHistory> histories = storyFetcher.getStoryHistories(teamOidToTeamName);

        OutputGenerator outputGenerator = new OutputGenerator();
        outputGenerator.createCsvFile(histories);
    }

}
