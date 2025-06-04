package com.access.versionone;

import com.versionone.apiclient.exceptions.V1Exception;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class V1Accessor {
    public static void main(String[] args) throws V1Exception, IOException, InterruptedException {
        Map<String, String> teamOidToTeamName = new ActivityFetcher().getTeamsToProcess();

        List<StoryHistory> histories = new StoryFetcher().getStoryHistories(teamOidToTeamName);

        new OutputGenerator(new FileWriter("stories.csv")).createCsvFile(histories,
                PropertyFetcher.getProperty("includeStoryPoints").equals("true"),
                PropertyFetcher.getProperty("includeTeamName").equals("true"));
    }
}
