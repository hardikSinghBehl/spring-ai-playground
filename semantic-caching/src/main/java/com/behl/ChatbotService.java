package com.behl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    private final ChatClient chatClient;
    private final SemanticCachingService semanticCachingService;

    ChatbotService(ChatClient chatClient, SemanticCachingService semanticCachingService) {
        this.chatClient = chatClient;
        this.semanticCachingService = semanticCachingService;
    }

    String chat(String question) {
        Optional<String> answerFromCache = semanticCachingService.search(question);
        if (answerFromCache.isPresent()) {
            log.info("Cache hit for question: {}", question);
            log.debug("Returning cached answer: {}", answerFromCache.get());
            return answerFromCache.get();
        }
        log.info("Cache miss for question: {}. Initiating RAG workflow.", question);

        String answer = chatClient
            .prompt(question)
            .call()
            .content();
        semanticCachingService.save(question, answer);
        log.info("Answer saved to cache for question: {}", question);
        return answer;
    }

}