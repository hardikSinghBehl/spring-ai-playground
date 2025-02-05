package com.behl;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@EnableConfigurationProperties(EvaluatorOptimizerProperties.class)
class EvaluatorOptimizerWorkflow {

    private final ChatbotService chatbotService;
    private final LLMResponseEvaluator llmResponseEvaluator;
    private final EvaluatorOptimizerProperties evaluatorOptimizerProperties;

    public EvaluatorOptimizerWorkflow(ChatbotService chatbotService, LLMResponseEvaluator llmResponseEvaluator, EvaluatorOptimizerProperties properties) {
        this.chatbotService = chatbotService;
        this.llmResponseEvaluator = llmResponseEvaluator;
        this.evaluatorOptimizerProperties = properties;
    }

    public String execute(String question) {
        ChatResponse currentResponse = null;
        EvaluationResponse evaluationResponse = null;
        int iterations = 0;

        do {
            Prompt prompt = constructUserPrompt(question, evaluationResponse, currentResponse);
            currentResponse = chatbotService.chat(prompt);
            evaluationResponse = llmResponseEvaluator.evaluate(question, currentResponse);
            iterations++;
        } while (shouldContinueOptimization(evaluationResponse, iterations));

        if (hasExhaustedMaxAttemptsWithoutSuccess(evaluationResponse.getScore(), iterations)) {
            throw new UnacceptableAnswerException();
        }
        return currentResponse.getResult().getOutput().getContent();
    }

    private Prompt constructUserPrompt(String question, EvaluationResponse previousEvaluation, ChatResponse currentResponse) {
        if (previousEvaluation == null) {
            return new Prompt(question);
        }
        Resource feedbackPrompt = evaluatorOptimizerProperties.optimizer().feedbackPrompt();
        PromptTemplate promptTemplate = new PromptTemplate(feedbackPrompt);
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("question", question);
        templateVariables.put("previousAnswer", currentResponse.getResult().getOutput().getContent());
        templateVariables.put("feedback", previousEvaluation.getFeedback());

        return promptTemplate.create(templateVariables);
    }

    private boolean shouldContinueOptimization(EvaluationResponse evaluationResponse, int iterations) {
        Float acceptableScore = evaluatorOptimizerProperties.evaluator().acceptableScore();
        Integer maxIterations = evaluatorOptimizerProperties.evaluator().maxIterations();
        return !evaluationResponse.isPass()
            && evaluationResponse.getScore() < acceptableScore
            && iterations < maxIterations;
    }

    private boolean hasExhaustedMaxAttemptsWithoutSuccess(Float currentScore, int iterations) {
        Float acceptableScore = evaluatorOptimizerProperties.evaluator().acceptableScore();
        Integer maxIterations = evaluatorOptimizerProperties.evaluator().maxIterations();
        return iterations >= maxIterations && currentScore < acceptableScore;
    }

}