package com.fedex.versionone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.versionone.Oid;
import com.versionone.apiclient.*;
import com.versionone.apiclient.exceptions.V1Exception;
import com.versionone.apiclient.interfaces.IAssetType;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class Access {
    //public static String ACCESS_TOKEN = "1.MhSfm87QEC1+gRwUdHaoEG3EFd4=";

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

        StoryFetcher fetcher = new StoryFetcher();
        List<String> stories = fetcher.getStoriesForTeam("Team:707462");

        ActivityFetcher activityFetcher = new ActivityFetcher();
        for (String storyId : stories) {
            String activity = activityFetcher.GetActivity2(storyId);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(activity);

            for (JsonNode node : root) {
                JsonNode body = node.get("body");
                JsonNode verb = body.get("verb");

                String verbText = verb.values().toString();
            }
        }
    }
}
