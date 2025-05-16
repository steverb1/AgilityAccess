package com.access.versionone;

import com.versionone.apiclient.V1Connector;
import com.versionone.apiclient.exceptions.V1Exception;

import java.io.IOException;

public class Connector {
    V1Connector buildV1Connector() throws V1Exception, IOException {
        String accessToken = PropertyFetcher.getProperty("v1.token");
        String baseUrl = PropertyFetcher.getProperty("v1.url");
        V1Connector.IAuthenticationMethods request = (V1Connector.IAuthenticationMethods) V1Connector.withInstanceUrl(baseUrl);

        return request.withAccessToken(accessToken).build();
    }
}
