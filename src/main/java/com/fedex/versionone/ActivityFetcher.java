package com.fedex.versionone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ActivityFetcher {
    StringBuffer GetActivity(String workItemId) throws IOException {
        String baseUrl = PropertyFetcher.getProperty("url");
        String accessToken = PropertyFetcher.getProperty("token");
        String urlString = baseUrl + "/api/ActivityStream/" + workItemId;
        URL url = URI.create(urlString).toURL();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " +  accessToken);
        con.setRequestProperty("Accept", "application/json");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response;
    }

    String GetActivity2(String workItemId) throws IOException, InterruptedException {
        String baseUrl = PropertyFetcher.getProperty("v1.url");
        String accessToken = PropertyFetcher.getProperty("v1.token");
        String urlString = baseUrl + "/api/ActivityStream/" + workItemId;

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        return response.body();
    }
}
