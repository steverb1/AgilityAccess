package com.access.versionone;

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

public class StoryFetcher {
    List<String> getStoriesForTeam(String teamOid) throws V1Exception, IOException {
        Connector connector = new Connector();
        V1Connector v1Connector = connector.buildV1Connector();

        MetaModel metaModel = new MetaModel(v1Connector);
        Services services = new Services(v1Connector);

        IAssetType storyType = metaModel.getAssetType("Story");
        Query query = new Query(storyType);

        IAttributeDefinition createDateAttr = storyType.getAttributeDefinition("CreateDate");
        LocalDate startDate = LocalDate.parse(PropertyFetcher.getProperty("startDate"));
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
