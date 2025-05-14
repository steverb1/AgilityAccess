package com.fedex.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.versionone.Oid;
import com.versionone.apiclient.*;
import com.versionone.apiclient.exceptions.V1Exception;
import com.versionone.apiclient.interfaces.IAssetType;

import java.io.IOException;
import java.util.List;

public class Access {
    public static void main(String[] args) throws V1Exception, IOException, InterruptedException {
        Connector connector = new Connector();
        V1Connector v1Connector = connector.buildV1Connector();

        Services services = new Services(v1Connector);
        MetaModel metaModel = new MetaModel(v1Connector);

        Oid storyOid = services.getOid("Story:20723163");

        IAssetType assetType = metaModel.getAssetType("Story");

        Query query = new Query(storyOid);
        query.getSelection().add(assetType.getAttributeDefinition("Name"));
        query.getSelection().add(assetType.getAttributeDefinition("Description"));

        ActivityFetcher activityFetcher = new ActivityFetcher();
        JsonNode root1 = activityFetcher.GetActivity(storyOid.toString());

        StoryFetcher fetcher = new StoryFetcher();
        List<String> stories = fetcher.getStoriesForTeam("Team:707462");

        for (String storyId : stories) {
            JsonNode root = activityFetcher.GetActivity(storyId);

            for (JsonNode node : root) {
                JsonNode body = node.get("body");
                JsonNode verb = body.get("verb");

                String verbText = verb.values().toString();
            }
        }
    }
}
