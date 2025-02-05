package com.behl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.Evaluator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@EnableConfigurationProperties(EvaluatorOptimizerProperties.class)
class LLMResponseEvaluator implements Evaluator {

    private final ChatClient contentEvaluator;
    private final EvaluatorOptimizerProperties evaluatorOptimizerProperties;

    public LLMResponseEvaluator(ChatClient contentEvaluator, EvaluatorOptimizerProperties evaluatorOptimizerProperties) {
        this.contentEvaluator = contentEvaluator;
        this.evaluatorOptimizerProperties = evaluatorOptimizerProperties;
    }

    public EvaluationResponse evaluate(String question, ChatResponse chatResponse) {
        String answer = chatResponse.getResult().getOutput().getContent();
        List<Document> documents = chatResponse.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        EvaluationRequest evaluationRequest = new EvaluationRequest(question, documents, answer);
        return evaluate(evaluationRequest);
    }

    @Override
    public EvaluationResponse evaluate(EvaluationRequest evaluationRequest) {
        Resource evaluationSystemPrompt = evaluatorOptimizerProperties.evaluator().systemPrompt();
        Prompt userPrompt = constructUserPrompt(evaluationRequest);
        LLMEvaluationResponse response = contentEvaluator
            .prompt()
            .system(evaluationSystemPrompt)
            .user(userPrompt.getContents())
            .call()
            .entity(LLMEvaluationResponse.class);
        return new EvaluationResponse(response.pass(), response.score(), response.feedback(), null);
    }

    private Prompt constructUserPrompt(EvaluationRequest evaluationRequest) {
        Resource evaluationUserPrompt = evaluatorOptimizerProperties.evaluator().userPrompt();
        String query = evaluationRequest.getUserText();
        String response = evaluationRequest.getResponseContent();
        String context = doGetSupportingData(evaluationRequest);

        PromptTemplate promptTemplate = new PromptTemplate(evaluationUserPrompt);
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("query", query);
        templateVariables.put("response", response);
        templateVariables.put("context", context);

        return promptTemplate.create(templateVariables);
    }

    /**
     * Custom record to capture the LLM's response, since {org.springframework.ai.evaluation.EvaluationResponse}
     * can not be deserialized.
     */
    record LLMEvaluationResponse(
        boolean pass,
        float score,
        String feedback) {
    }

}