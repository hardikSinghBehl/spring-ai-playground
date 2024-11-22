package com.behl.recommender;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VectorStoreInitializer implements ApplicationRunner {

    private final VectorStore vectorStore;
    private final MovieFetcher movieFetcher;

    public VectorStoreInitializer(VectorStore vectorStore, MovieFetcher movieFetcher) {
        this.vectorStore = vectorStore;
        this.movieFetcher = movieFetcher;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Document> documents = movieFetcher
            .fetch()
            .stream()
            .map(movie -> {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("title", movie.title());
                metadata.put("year", movie.year());
                metadata.put("language", movie.language());
                return new Document(movie.plot(), metadata);
            })
            .toList();
        vectorStore.add(documents);
    }

}