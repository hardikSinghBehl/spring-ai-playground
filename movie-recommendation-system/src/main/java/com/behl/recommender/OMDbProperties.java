package com.behl.recommender;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.behl.recommender.omdb")
public record OMDbProperties(String baseUrl, String apiKey) {
}