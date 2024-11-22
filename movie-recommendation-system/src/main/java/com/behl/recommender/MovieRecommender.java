package com.behl.recommender;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MovieRecommender {

    private static final int MAX_RESULTS = 3;

    private final VectorStore vectorStore;

    public MovieRecommender(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Movie> recommend(String query) {
        SearchRequest searchRequest = SearchRequest.query(query).withTopK(MAX_RESULTS);
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        return documents.stream().map(this::convert).toList();
    }

    private Movie convert(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        String title = String.valueOf(metadata.get("title"));
        String year = String.valueOf(metadata.get("year"));
        String language = String.valueOf(metadata.get("language"));
        String plot = document.getContent();
        return new Movie(title, year, language, plot);
    }

}