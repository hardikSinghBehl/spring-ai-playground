package com.behl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EvaluatorOptimizerProperties.class)
class LLMConfiguration {

    /**
     * In-memory vector store for proof-of-concept.
     * Not to be used in production.
     */
    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore
            .builder(embeddingModel)
            .build();
    }

    @Bean
    ChatClient contentGenerator(ChatModel ollamaChatModel, VectorStore vectorStore) {
        return ChatClient.builder(ollamaChatModel)
            .defaultAdvisors(
                new QuestionAnswerAdvisor(vectorStore),
                new SimpleLoggerAdvisor()
            )
            .build();
    }

    /**
     * Manually creating a ChatModel bean for the evaluation ChatClient, since OllamaAutoConfiguration only allows
     * a single model to be configured via the spring.ai.ollama.chat.options.model property currently.
     */
    @Bean
    ChatModel evaluationChatModel(OllamaApi ollamaApi, EvaluatorOptimizerProperties evaluatorOptimizerProperties) {
        String evaluationModel = evaluatorOptimizerProperties.evaluator().model();
        return OllamaChatModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(OllamaOptions.builder()
                .model(evaluationModel)
                .build())
            .modelManagementOptions(ModelManagementOptions.builder()
                .pullModelStrategy(PullModelStrategy.WHEN_MISSING)
                .build())
            .build();
    }

    @Bean
    ChatClient contentEvaluator(ChatModel evaluationChatModel) {
        return ChatClient
            .builder(evaluationChatModel)
            .defaultAdvisors(new SimpleLoggerAdvisor())
            .build();
    }

}