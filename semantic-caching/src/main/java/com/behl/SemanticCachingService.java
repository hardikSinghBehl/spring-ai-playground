package com.behl;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@EnableConfigurationProperties(SemanticCacheProperties.class)
class SemanticCachingService {

    private final RedisVectorStore redisVectorStore;
    private final SemanticCacheProperties semanticCacheProperties;

    SemanticCachingService(RedisVectorStore redisVectorStore, SemanticCacheProperties semanticCacheProperties) {
        this.redisVectorStore = redisVectorStore;
        this.semanticCacheProperties = semanticCacheProperties;
    }

    void save(String question, String answer) {
        var document = Document
            .builder()
            .text(question)
            .metadata(semanticCacheProperties.metadataField(), answer)
            .build();
        redisVectorStore.add(List.of(document));
    }

    Optional<String> search(String question) {
        var searchRequest = SearchRequest.builder()
            .query(question)
            .similarityThreshold(semanticCacheProperties.similarityThreshold())
            .topK(1)
            .build();
        var results = redisVectorStore.similaritySearch(searchRequest);

        if (results.isEmpty()) {
            return Optional.empty();
        }

        var result = results.getFirst();
        var answer = String.valueOf(result.getMetadata().get(semanticCacheProperties.metadataField()));
        return Optional.of(answer);
    }

}