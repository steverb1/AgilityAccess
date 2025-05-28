package com.access.versionone;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientWrapper implements ForHttpClientCalls {
    private final HttpClient httpClient;

    public HttpClientWrapper() {
        httpClient = HttpClient.newHttpClient();
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
        return httpClient.send(request, responseBodyHandler);
    }
}
