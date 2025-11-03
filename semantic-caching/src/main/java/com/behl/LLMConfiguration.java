package com.behl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pinecone.PineconeVectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
@EnableConfigurationProperties(SemanticCacheProperties.class)
class LLMConfiguration {

    @Bean
    ChatClient chatClient(ChatModel chatModel, PineconeVectorStore pineconeVectorStore) {
        return ChatClient
            .builder(chatModel)
            .defaultAdvisors(
                new QuestionAnswerAdvisor(pineconeVectorStore)
            )
            .build();
    }

    @Bean
    JedisPooled jedisPooled(RedisProperties redisProperties) {
        return new JedisPooled(redisProperties.getUrl());
    }

    @Bean
    RedisVectorStore redisVectorStore(
        JedisPooled jedisPooled,
        EmbeddingModel embeddingModel,
        SemanticCacheProperties semanticCacheProperties
    ) {
        return RedisVectorStore
            .builder(jedisPooled, embeddingModel)
            .contentFieldName(semanticCacheProperties.contentField())
            .embeddingFieldName(semanticCacheProperties.embeddingField())
            .metadataFields(
                RedisVectorStore.MetadataField.text(semanticCacheProperties.metadataField()))
            .initializeSchema(true)
            .build();
    }

}