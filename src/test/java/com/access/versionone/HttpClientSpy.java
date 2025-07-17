package com.access.versionone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HttpClientSpy implements ForHttpClientCalls {
    int callCount = -1;

    List<String> body = new ArrayList<>();
    HttpRequest lastRequest;

    public void addBody(String body) {
        this.body.add(body);
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        lastRequest = request;
        callCount++;

        return new HttpResponse<>() {
            @Override public int statusCode() {return 200;}
            @Override public HttpRequest request() {return request;}
            @Override public Optional<HttpResponse<T>> previousResponse() {return Optional.empty();}
            @Override public T body() {return (T) body.get(callCount);}
            @Override public Optional<SSLSession> sslSession() {return Optional.empty();}
            @Override public URI uri() {return null;}
            @Override public HttpClient.Version version() {return null;}
            @Override public HttpHeaders headers() {return null;}
        };
    }

    public JsonNode sendHttpRequest(String urlString) throws JsonProcessingException {
        lastRequest = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer exampleAccessToken").build();
        HttpResponse<String> response = send(lastRequest, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(response.body());
    }
}
