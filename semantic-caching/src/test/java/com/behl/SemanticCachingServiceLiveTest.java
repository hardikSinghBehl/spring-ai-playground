package com.behl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariables({
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".*"),
    @EnabledIfEnvironmentVariable(named = "REDIS_URL", matches = ".*")
})
class SemanticCachingServiceLiveTest {

    @Autowired
    private SemanticCachingService semanticCachingService;

    @Test
    void whenUsingSemanticCache_thenCacheReturnsAnswerForSemanticallyRelatedQuestion() {
        var question = "How many sick leaves can I take?";
        var answer = "No leaves allowed for slaves! Get back to work!!";
        semanticCachingService.save(question, answer);

        var rephrasedQuestion = "How many days sick leave can I take?";
        assertThat(semanticCachingService.search(rephrasedQuestion))
            .isPresent()
            .hasValue(answer);

        var unrelatedQuestion = "Can I get a raise?";
        assertThat(semanticCachingService.search(unrelatedQuestion))
            .isEmpty();
    }

}