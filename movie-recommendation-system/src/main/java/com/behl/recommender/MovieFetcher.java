package com.behl.recommender;

import net.datafaker.Faker;
import net.datafaker.providers.base.Unique;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class MovieFetcher {

    private static final int DEFAULT_COUNT = 20;
    private static final Logger log = LoggerFactory.getLogger(MovieFetcher.class);

    private final OMDbClient omDbClient;

    public MovieFetcher(OMDbClient omDbClient) {
        this.omDbClient = omDbClient;
    }

    public List<Movie> fetch() {
        return fetch(DEFAULT_COUNT);
    }

    public List<Movie> fetch(int count) {
        Unique unique = new Faker().unique();
        List<Movie> movies = new ArrayList<>();
        return IntStream.range(0, count)
            .parallel()
            .mapToObj(i -> unique.fetchFromYaml("movie.name"))
            .map(omDbClient::getMovie)
            .peek(movie -> log.debug("Fetched: {}", movie))
            .toList();
    }

}