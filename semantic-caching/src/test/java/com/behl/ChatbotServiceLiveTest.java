package com.behl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariables;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import redis.clients.jedis.JedisPooled;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariables({
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".*"),
    @EnabledIfEnvironmentVariable(named = "PINECONE_API_KEY", matches = ".*"),
    @EnabledIfEnvironmentVariable(named = "PINECONE_DB_INDEX_NAME", matches = ".*"),
    @EnabledIfEnvironmentVariable(named = "REDIS_URL", matches = ".*")
})
@ActiveProfiles("populate-db")
@ExtendWith(OutputCaptureExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChatbotServiceLiveTest {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private JedisPooled jedisPooled;

    @Autowired
    private PineconeDBUtility pineconeDBUtility;

    @BeforeEach
    void setup() {
        jedisPooled
            .keys("embedding:*")
            .forEach(jedisPooled::del);
    }

    @AfterAll
    void cleanUp() {
        pineconeDBUtility.clear();
    }

    @Test
    void whenQueryingSemanticallyRelatedQuestions_thenCacheHitOccursForSecondQuery(CapturedOutput capturedOutput) {
        var question = "Can I get access to courses from Coursera for upskilling?";
        var answer = chatbotService.chat(question);

        assertThat(answer)
            .isNotNull()
            .isNotEmpty();
        assertThat(capturedOutput.getOut())
            .contains(
                "Cache miss for question: " + question,
                "Answer saved to cache for question: " + question
            );

        var rephrasedQuestion = "Do I have access to Coursera courses for learning?";
        var answerToRephrasedQuestion = chatbotService.chat(rephrasedQuestion);

        assertThat(answerToRephrasedQuestion)
            .isEqualTo(answer);
        assertThat(capturedOutput.getOut())
            .contains(
                "Cache hit for question: " + rephrasedQuestion,
                "Returning cached answer: " + answer
            );
    }

}