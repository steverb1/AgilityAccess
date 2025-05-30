package com.access.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.versionone.apiclient.*;
import com.versionone.apiclient.exceptions.V1Exception;
import com.versionone.apiclient.filters.AndFilterTerm;
import com.versionone.apiclient.filters.FilterTerm;
import com.versionone.apiclient.interfaces.IAssetType;
import com.versionone.apiclient.interfaces.IAttributeDefinition;
import com.versionone.apiclient.services.QueryResult;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StoryFetcher {
    ForHttpClientCalls httpClient = new HttpClientWrapper();

    List<StoryHistory> getStoryHistories(Map<String, String> teamOidToTeamName) throws V1Exception, IOException, InterruptedException {
        List<StoryHistory> histories = new ArrayList<>();

        for (String teamOid : teamOidToTeamName.keySet()) {
            List<String> storyIds = getStoriesForTeam(teamOid);

            ActivityFetcher activityFetcher = new ActivityFetcher(httpClient);

            Float storyPoints = null;
            String teamName = "";

            for (String storyId : storyIds) {
                JsonNode storyRoot = activityFetcher.getActivity(storyId, PropertyFetcher.getProperty("v1.url"), PropertyFetcher.getProperty("v1.token"));

                StoryParser storyParser = new StoryParser(storyRoot);
                Map<String, LocalDate> storyDates = storyParser.findStateChangeDates();

                if (PropertyFetcher.getProperty("includeStoryPoints").equals("true")) {
                    storyPoints = storyParser.findStoryEstimate();
                }
                if (PropertyFetcher.getProperty("includeTeamName").equals("true")) {
                    teamName = teamOidToTeamName.get(teamOid);
                }

                StoryHistory storyHistory = new StoryHistory(storyId, storyDates, storyPoints, teamName);
                histories.add(storyHistory);
            }
        }
        return histories;
    }

    private List<String> getStoriesForTeam(String teamOid) throws V1Exception, IOException {
        Connector connector = new Connector();
        V1Connector v1Connector = connector.buildV1Connector();

        MetaModel metaModel = new MetaModel(v1Connector);
        Services services = new Services(v1Connector);

        IAssetType storyType = metaModel.getAssetType("Story");
        Query query = new Query(storyType);

        IAttributeDefinition createDateAttr = storyType.getAttributeDefinition("ClosedDate");
        LocalDate startDate = LocalDate.parse(PropertyFetcher.getProperty("fromClosedDate"));
        Date since = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        FilterTerm dateFilter = new FilterTerm(createDateAttr);
        dateFilter.greater(since);

        IAttributeDefinition teamFilterAttr = storyType.getAttributeDefinition("Team");
        FilterTerm teamFilter = new FilterTerm(teamFilterAttr);
        teamFilter.equal(teamOid);

        AndFilterTerm combined = new AndFilterTerm(dateFilter, teamFilter);
        query.setFilter(combined);

        QueryResult result = services.retrieve(query);

        List<String> stories = new ArrayList<>();
        for (Asset story : result.getAssets()) {
            stories.add(story.getOid().toString());
        }

        return stories;
    }
}
