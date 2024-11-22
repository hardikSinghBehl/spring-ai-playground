package com.behl.recommender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@EnableConfigurationProperties(OMDbProperties.class)
public class OMDbClient {

    private final OMDbProperties omDbProperties;
    private final RestClient restClient;

    public OMDbClient(OMDbProperties omDbProperties) {
        this.omDbProperties = omDbProperties;
        this.restClient = RestClient.builder().baseUrl(omDbProperties.baseUrl()).build();
    }

    public Movie getMovie(String movieName) {
        return restClient.get()
            .uri(uriBuilder -> uriBuilder
                .queryParam("apikey", omDbProperties.apiKey())
                .queryParam("t", movieName)
                .queryParam("plot", "full")
                .build())
            .retrieve()
            .body(Movie.class);
    }

}