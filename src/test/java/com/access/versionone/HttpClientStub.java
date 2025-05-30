package com.access.versionone;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class HttpClientStub implements ForHttpClientCalls {
    String body;
    HttpRequest lastRequest;

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        lastRequest = request;

        return new HttpResponse<>() {
            @Override public int statusCode() {return 200;}
            @Override public HttpRequest request() {return request;}
            @Override public Optional<HttpResponse<T>> previousResponse() {return Optional.empty();}
            @Override public T body() {return (T) body;}
            @Override public Optional<SSLSession> sslSession() {return Optional.empty();}
            @Override public URI uri() {return null;}
            @Override public HttpClient.Version version() {return null;}
            @Override public HttpHeaders headers() {return null;}
        };
    }
}
