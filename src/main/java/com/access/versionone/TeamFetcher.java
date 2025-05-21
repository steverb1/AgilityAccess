package com.access.versionone;

import com.versionone.Oid;
import com.versionone.apiclient.*;
import com.versionone.apiclient.exceptions.V1Exception;
import com.versionone.apiclient.services.QueryResult;

import java.io.IOException;

public class TeamFetcher {
    String getTeamName(String teamOidString) throws V1Exception, IOException {
        Connector connector = new Connector();
        V1Connector v1Connector = connector.buildV1Connector();
        MetaModel metaModel = new MetaModel(v1Connector);
        Services services = new Services(v1Connector);

        Oid teamOid = Oid.fromToken(teamOidString, metaModel);

        Query query = new Query(teamOid);
        query.getSelection().add(metaModel.getAttributeDefinition("Team.Name"));

        QueryResult result = services.retrieve(query);
        Asset teamAsset = result.getAssets()[0];

        return teamAsset.getAttribute(metaModel.getAttributeDefinition("Team.Name")).getValue().toString();
    }
}
